package com.doubtnutapp.ui.groupChat

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.remote.ResponseMeta
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.addEventNames
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.data.remote.models.GroupChatMessages
import com.doubtnutapp.data.remote.models.GroupListData
import com.doubtnutapp.data.remote.repository.GroupChatRepository
import com.doubtnutapp.doAsyncPost
import com.doubtnutapp.toRequestBody
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.HashMap

class GroupChatViewModel @Inject constructor(
    app: Application,
    private val analyticsPublisher: AnalyticsPublisher,
    private val groupChatRepository: GroupChatRepository,
    compositeDisposable: CompositeDisposable,
) : BaseViewModel(compositeDisposable) {

    private val chatListLiveData: MutableLiveData<Outcome<ArrayList<Comment>>> = MutableLiveData()
    private var cursorData: String = "0"
    private var totalEngagementTime: Int = 0
    private var engamentTimeToSend: Number = 0
    private var timerTask: TimerTask? = null
    private var engageTimer: Timer? = null
    private var handler: Handler? = null
    private var timeFormatter = SimpleDateFormat("m:ss", Locale.getDefault())
    var isApplicationBackground = false
    fun chatListLiveData() = chatListLiveData

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()

        engageTimer?.cancel()
        engageTimer = null
        timerTask?.cancel()
        timerTask = null
    }

    fun getGroupList(): RetrofitLiveData<ApiResponse<ArrayList<GroupListData>>> {
        return groupChatRepository.getGroupList()
    }

    fun addComment(
        entityType: String,
        entityId: String,
        message: String,
        imageFile: File?,
        file: File?
    ): RetrofitLiveData<ApiResponse<Comment>> {
        return groupChatRepository.addComment(
            entityType,
            entityId,
            message,
            getImageFileMultiPartRequestBody(imageFile),
            getAudioFileMultiPartRequestBody(file)
        )
    }

    fun reportComment(commentId: String): RetrofitLiveData<ApiResponse<Any>> {
        return groupChatRepository.reportComment(commentId, getReportPostRequestBody())
    }

    var imageString: String = ""

    fun getBitmapAsString(uri: Uri, callback: (imageAsString: String) -> Unit) {
        doAsyncPost(handler = {
            imageString = getEncodedImage(uri)
        }, postHandler = {
            callback(imageString)
        }).execute()
    }

    private fun getEncodedImage(imageUri: Uri): String {
        val bmp = MediaStore.Images.Media.getBitmap(
            DoubtnutApp.INSTANCE.contentResolver,
            imageUri
        )
        val byteCount = bmp.byteCount.toFloat()
        val bitWidth = bmp.width.toFloat()
        val bitHeight = bmp.height.toFloat()
        val bitPer = byteCount * 8 / (bitWidth * bitHeight)

        val baos = ByteArrayOutputStream()
        if (bitPer >= 16.0) {
            bmp.compress(
                Bitmap.CompressFormat.JPEG,
                (100.0 * (16.0 / bitPer).toFloat()).toInt(),
                baos
            )
        } else {
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        }

        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun getAudioFileMultiPartRequestBody(audioFile: File?): MultipartBody.Part? {
        val audioSend = audioFile?.asRequestBody("audio/*".toMediaTypeOrNull())
        return if (audioSend != null) MultipartBody.Part.createFormData(
            "audio",
            audioFile.name,
            audioSend
        ) else null
    }

    private fun getImageFileMultiPartRequestBody(imageFile: File?): MultipartBody.Part? {
        val audioSend = imageFile?.asRequestBody("image/*".toMediaTypeOrNull())
        return if (audioSend != null) MultipartBody.Part.createFormData(
            "image",
            imageFile.name,
            audioSend
        ) else null
    }

    private fun getReportPostRequestBody(): RequestBody {
        return HashMap<String, Any>().apply {
            this["entity_type"] = "group_chat"
            this["message"] = "not"
        }.toRequestBody()
    }

    fun getChatListData(token: String, groupId: String) {
        chatListLiveData.value = Outcome.loading(true)
        compositeDisposable.add(Observable.interval(0, 10, TimeUnit.SECONDS)
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .switchMap {
                groupChatRepository.getLiveChatData(groupId, cursorData)
                    .onErrorResumeNext(
                        Observable.just(
                            ApiResponse(
                                ResponseMeta(0, "", null),
                                GroupChatMessages(arrayListOf(), "0"), null
                            )
                        )
                    )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                chatListLiveData.value = Outcome.loading(false)
                chatListLiveData.value = Outcome.success(it.data.MessagesList)
                cursorData = it.data.cursorData

            }, {
                chatListLiveData.value = Outcome.loading(false)
                parseErrorAndNotify(it)
            })
        )
    }

    fun onStop() {
        timerTask?.let { handler?.removeCallbacks(it) }
        this.sendEventForEngagement(EventConstants.GUPSHUP_ENGAGEMENT, engamentTimeToSend)
        val event = StructuredEvent(
            action = EventConstants.GUPSHUP_ENGAGEMENT,
            category = EventConstants.GROUP_CHAT,
            value = engamentTimeToSend.toDouble(),
            eventParams = hashMapOf(
                EventConstants.SOURCE to EventConstants.GROUP_CHAT,
                EventConstants.PARAM_TIMESTAMP to System.currentTimeMillis()
            )
        )
        this.eventWith(event)
        totalEngagementTime = 0
    }

    fun setupEngagementTracking() {
        handler = Handler(Looper.getMainLooper())
        startEngagementTimer()
    }

    private fun startEngagementTimer() {
        if (engageTimer == null) {
            engageTimer = Timer()
            timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
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

    private fun parseErrorAndNotify(it: Throwable?) {
        if (it is HttpException) {
            when (it.code()) {
                401 -> chatListLiveData.value =
                    Outcome.badRequest(it.response()?.message().orEmpty())
                403 -> chatListLiveData.value =
                    Outcome.apiError(Throwable(it.response()?.message()))
                else -> chatListLiveData.value = Outcome.failure(it)
            }
        }
    }

    fun disposeCall() {
        compositeDisposable.clear()
    }

    fun eventWith(snowplowEvent: StructuredEvent) {
        analyticsPublisher.publishEvent(snowplowEvent)
    }

    fun eventWith(eventName: String, params: HashMap<String, Any>? = null, ignoreSnowplow: Boolean = false) {
        val event = AnalyticsEvent(eventName, params ?: hashMapOf(), ignoreSnowplow = ignoreSnowplow)
        analyticsPublisher.publishEvent(event)
    }

    private fun sendEventForEngagement(eventName: String, engagementTimeToSend: Number) {
        val app = DoubtnutApp.INSTANCE
        app.getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(app.applicationContext).toString())
            .addStudentId(getStudentId())
            .addScreenName(EventConstants.PAGE_GUPSHUP)
            .addEventParameter(EventConstants.GUPSHUP_ENGAGEMENT_TOTAL_TIME, engagementTimeToSend)
            .track()

    }

}