package com.doubtnutapp.ui.userstatus

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.EventBus.StatusCreated
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.ActivityCreateStatusBinding
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.theartofdev.edmodo.cropper.CropImage
import dagger.android.AndroidInjection
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


class CreateStatusActivity : AppCompatActivity() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var compositeDisposable = CompositeDisposable()
    private val compositeUploadRequests = CompositeDisposable()
    private var caption = ""
    private var uploadUrl = ""
    private var imageSource = ""

    private var isRotate = false
    private var isCropped = false
    private var mImageSource: String = ""

    private lateinit var binding: ActivityCreateStatusBinding


    private var appStateObserver: Disposable? = null

    private var totalEngagementTime: Int = 0
    private var engamentTimeToSend: Number = 0
    private var timerTask: TimerTask? = null
    private var engageTimer: Timer? = null
    private var handler: Handler? = null
    var isApplicationBackground = false


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityCreateStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEngagementTracking()
        appStateObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is ApplicationStateEvent) {
                isApplicationBackground = !event.state
            }
        }
        sendAddStatusClickEvent()
        CameraActivity.getStartIntent(this, "status").apply {
            startActivityForResult(this, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            onCropError(data)
            sendAddStatusCancelledEvent()
            return
        }
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {

                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val result = CropImage.getActivityResult(data)
                        isRotate = result.rotation != 0
                        isCropped = data.getBooleanExtra("cropped", false)
                        mImageSource = data.getStringExtra(Constants.IMAGE_SOURCE).orEmpty()
                        createStatus(data)
                    }

                    Activity.RESULT_CANCELED -> onCancelFromCrop()
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                onCropError(data)
            }
        }
    }

    private fun createStatus(data: Intent?) {
        if (data == null) {
            toast("Something went wrong !!")
            finishActivity()
            return
        }
        //
        binding.layoutUploadProgress.visibility = View.VISIBLE
        caption = data.getStringExtra("caption").orEmpty()

        val result = CropImage.getActivityResult(data)
        uploadAttachment(result)

    }

    private fun uploadAttachment(result: CropImage.ActivityResult?) {
        if (result == null || result.uri == null) {
            toast("Something went wrong !!")
            finishActivity()
            return
        }
        sendStatusUploadInitiatedEvent()
        val imageUri = result.uri
        val uploadRequest = DataHandler.INSTANCE.teslaRepository.getSignedUrl("image/png", imageUri.lastPathSegment
                ?: "", Utils.getFileExtension(imageUri.toString()), Utils.getMimeType(imageUri))
                .subscribeOn(Schedulers.io())
                .flatMap { signUrlResponse ->
                    if (signUrlResponse.error == null && signUrlResponse.meta.code == 200) {
                        uploadUrl = signUrlResponse.data.fileName
                        val imageFile = File(imageUri.path)

                        val fileBody = ProgressRequestBody(imageFile, "application/octet", object : ProgressRequestBody.UploadProgressListener {
                            override fun onProgressUpdate(percentage: Int) {
                            }
                        })
                        DataHandler.INSTANCE.teslaRepository.uploadAttachment(signUrlResponse.data.url, fileBody)
                    } else {
                        sendStatusUploadFailedEvent()
                        toast("Some Error occurred in uploading image. Please try again")
                        finishActivity()
                        null
                    }
                }

        compositeUploadRequests.add(Single.zip(listOf(uploadRequest)) {
            it
        }.subscribe({
            submitStatus(uploadUrl)
        }) {
            sendStatusUploadFailedEvent()
            toast("Some Error occurred in uploading image. Please try again")
            finishActivity()
        })
    }

    private fun submitStatus(attachmentUrl: String?) {
        if (attachmentUrl.isNullOrEmpty()) {
            sendStatusUploadFailedEvent()
            toast("Error creating status")
            finishActivity()
            return
        }
        val body = HashMap<String, Any>().apply {
            this["caption"] = caption
            this["type"] = "image"
            this["attachment"] = attachmentUrl
        }
        val observer = DataHandler.INSTANCE.microService.get().addStory(body.toRequestBody()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.meta.success == "true") {
                        toast("Your status has been successfully posted...")
                        DoubtnutApp.INSTANCE.bus()?.send(StatusCreated(it.data))
                        sendStatusUploadSuccessEvent()
                        finishActivity()
                    } else {
                        if (it.data.isOverflow == true) {
                            showOverflowErrorDialog(it.meta.message)
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.STATUS_CREATION_LIMIT_REACHED, ignoreSnowplow = true, ignoreFirebase = false))
                            analyticsPublisher.publishEvent(StructuredEvent(EventConstants.CATEGORY_STATUS, EventConstants.STATUS_CREATION_LIMIT_REACHED, eventParams = hashMapOf<String, Any>().apply {
                                put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                            }))
                        } else {
                            showOverflowErrorDialog("Error in posting status... Please try again later")
                        }
                    }
                }, {
                    sendStatusUploadFailedEvent()
                    apiErrorToast(it, Toast.LENGTH_SHORT)
                    finishActivity()
                })

        compositeDisposable.add(observer)

    }

    private fun showOverflowErrorDialog(message: String) {
        binding.layoutUploadProgress.visibility = View.GONE
        val builder = AlertDialog.Builder(this)
        //set message for alert dialog
        builder.setMessage(message)

        builder.setPositiveButton("OK") { dialogInterface, which ->
            finishActivity()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun onCancelFromCrop() {
        finishActivity()
    }

    private fun onCropError(data: Intent?) {
        finishActivity()
    }

    fun finishActivity() {
        finish()
    }

    private fun sendStatusUploadInitiatedEvent() {
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
            put(EventConstants.ROTATE, isRotate)
            put(EventConstants.CROPPED, isCropped)
            put(EventConstants.IMAGE_SOURCE, mImageSource)
        }
        analyticsPublisher.publishEvent(StructuredEvent(EventConstants.CATEGORY_STATUS,
                EventConstants.STATUS_UPLOAD_INITIATED,
                eventParams = params)
        )
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.STATUS_UPLOAD_INITIATED, params = params, ignoreFirebase = false, ignoreApxor = false, ignoreSnowplow = true))

    }

    private fun sendStatusUploadFailedEvent() {
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
            put(EventConstants.UPLOAD_URL, uploadUrl)
            put(EventConstants.ROTATE, isRotate)
            put(EventConstants.CROPPED, isCropped)
            put(EventConstants.IMAGE_SOURCE, mImageSource)
        }
        analyticsPublisher.publishEvent(StructuredEvent(EventConstants.CATEGORY_STATUS,
                EventConstants.STATUS_UPLOAD_FAILURE,
                eventParams = params)
        )
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.STATUS_UPLOAD_FAILURE, params = params, ignoreFirebase = false, ignoreApxor = false, ignoreSnowplow = true))

    }

    private fun sendStatusUploadSuccessEvent() {
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
            put(EventConstants.UPLOAD_URL, uploadUrl)
            put(EventConstants.ROTATE, isRotate)
            put(EventConstants.CROPPED, isCropped)
            put(EventConstants.IMAGE_SOURCE, mImageSource)
        }
        analyticsPublisher.publishEvent(StructuredEvent(EventConstants.CATEGORY_STATUS,
                EventConstants.STATUS_UPLOAD_SUCCESS,
                eventParams = params)
        )
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.STATUS_UPLOAD_SUCCESS, params = params, ignoreFirebase = false, ignoreApxor = false, ignoreSnowplow = true))

    }

    private fun sendAddStatusClickEvent() {
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
        }
        analyticsPublisher.publishEvent(StructuredEvent(EventConstants.CATEGORY_STATUS,
                EventConstants.ADD_STATUS_CLICKED,
                eventParams = params)
        )
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ADD_STATUS_CLICKED, params = params, ignoreFirebase = false, ignoreApxor = false, ignoreSnowplow = true))

    }

    private fun sendAddStatusCancelledEvent() {
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
        }

        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ADD_STATUS_CANCELLED, params = params, ignoreFirebase = false,
            ignoreApxor = false, ignoreSnowplow = true))

    }

    companion object {

        fun getStartIntent(
                context: Context,
                source: String
        ) = Intent(context, CreateStatusActivity::class.java).apply {
            putExtra(Constants.SOURCE, source)
        }
    }

    override fun onStop() {
        super.onStop()
        timerTask?.let { handler?.removeCallbacks(it) }
        sendEventForEngagement(EventConstants.EVENT_STATUS_ENGAGEMENT, engamentTimeToSend)

        analyticsPublisher.publishEvent(StructuredEvent(action = EventConstants.EVENT_STATUS_ENGAGEMENT,
                value = engamentTimeToSend.toDouble(),
                category = EventConstants.CATEGORY_STATUS,
                eventParams = hashMapOf(
                        EventConstants.PARAM_TIMESTAMP to System.currentTimeMillis(),
                        EventConstants.SOURCE to EventConstants.SOURCE_STATUS_ENGAGEMENT_CREATE_STATUS
                )))

        totalEngagementTime = 0
    }

    private fun setupEngagementTracking() {
        handler = Handler(Looper.getMainLooper())
        startEngagementTimer()
    }

    private fun startEngagementTimer() {
        if (engageTimer == null) {
            engageTimer = Timer()
        }
        timerTask = object : TimerTask() {
            override fun run() {
                handler?.post {
                    if (!isApplicationBackground) {
                        engamentTimeToSend = totalEngagementTime
                        totalEngagementTime++
                    }
                }
            }
        }
        totalEngagementTime = 0
        engageTimer!!.schedule(timerTask, 0, 1000)
    }

    private fun sendEventForEngagement(eventName: String, engagementTimeToSend: Number) {

        val app = DoubtnutApp.INSTANCE
        app.getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(app.applicationContext).toString())
                .addStudentId(UserUtil.getStudentId())
                .addScreenName("CreateStatus")
                .addEventParameter(EventConstants.ENGAGEMENT_TIME, engagementTimeToSend)
                .track()

    }

    override fun onDestroy() {
        super.onDestroy()
        engageTimer?.cancel()
        engageTimer = null

        timerTask?.cancel()
        timerTask = null

        appStateObserver?.dispose()
        compositeDisposable?.dispose()
        compositeUploadRequests?.dispose()
    }
}