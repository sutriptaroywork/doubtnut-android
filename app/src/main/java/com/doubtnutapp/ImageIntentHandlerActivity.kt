package com.doubtnutapp

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.camera.viewmodel.CameraActivityViewModel
import com.doubtnutapp.databinding.ActivityImageIntentHandlerBinding
import com.doubtnutapp.matchquestion.ui.activity.MatchQuestionActivity
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.facebook.appevents.AppEventsConstants
import com.theartofdev.edmodo.cropper.CropConstant
import com.theartofdev.edmodo.cropper.CropImage
import java.io.File
import javax.inject.Inject

class ImageIntentHandlerActivity :
    BaseBindingActivity<CameraActivityViewModel, ActivityImageIntentHandlerBinding>() {

    companion object {
        const val TAG = "ImageIntentHandlerActivity"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private fun openSplashActivity() {
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun sendEvent(eventName: String) {
        (applicationContext as DoubtnutApp)
            .getEventTracker()
            .addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(this).toString())
            .addStudentId(getStudentId())
            .addScreenName("ImageIntentHandlerActivity")
            .track()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            return
        }

        val result = CropImage.getActivityResult(data)

        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    this@ImageIntentHandlerActivity.let {
                        val countToSendEvent: Int = Utils.getCountToSend(
                            RemoteConfigUtils.getEventInfo(),
                            EventConstants.QUESTION_ASK
                        )
                        repeat((0 until countToSendEvent).count()) {
                            sendEvent(EventConstants.QUESTION_ASK)
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    AppEventsConstants.EVENT_NAME_SUBMIT_APPLICATION,
                                    hashMapOf()
                                )
                            )
                        }
                        MatchQuestionActivity.getStartIntent(
                            it, result.uri.path
                                ?: "", "", ""
                        ).apply {
                            startActivity(this)
                        }
                        it.overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK)
                        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK)

                        viewModel.sendEvent(
                            EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.SOURCE, EventConstants.IMAGE_SHARE_CROP_ACTIVITY)
                                put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                            },
                            true
                        )

                        it.finish()
                    }

                } else if (resultCode == 0) {
                    val params = Bundle()
                    params.putString("student_id", getStudentId())
                    viewModel.sendEvent(
                        EventConstants.EVENT_NAME_CLOSE_FROM_CROP,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.SOURCE, EventConstants.IMAGE_SHARE_CROP_ACTIVITY)
                        })
                }

            }

            CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                val cropError: Throwable? = result.error
                if (cropError != null) {
                    ToastUtils.makeText(this, cropError.message ?: "", Toast.LENGTH_LONG).show()
                } else {
                    ToastUtils.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    override fun provideViewBinding(): ActivityImageIntentHandlerBinding {
        return ActivityImageIntentHandlerBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): CameraActivityViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_APP_OPEN_DN)
        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_APP_OPEN_DN)

        viewModel.sendEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, hashMapOf<String, Any>().apply {
            put(EventConstants.SOURCE, EventConstants.APP_OPEN_IMAGE_SHARE)
        }, true)

        analyticsPublisher.publishEvent(
            StructuredEvent(category = EventConstants.EVENT_NAME_APP_OPEN_DN_CATEGORY,
                action = EventConstants.EVENT_NAME_APP_OPEN_DN,
                eventParams = hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, EventConstants.APP_OPEN_IMAGE_SHARE)
                })
        )

        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(EventConstants.EVENT_NAME_APP_OPEN_DN)
            .addEventParameter(EventConstants.SOURCE, EventConstants.APP_OPEN_IMAGE_SHARE)
            .addStudentId(UserUtil.getStudentId())
            .track()

//        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishBranchIoEvent(
//            AnalyticsEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, hashMapOf<String, Any>().apply {
//                put(EventConstants.SOURCE, EventConstants.APP_OPEN_IMAGE_SHARE)
//            })
//        )

        if (intent?.type?.startsWith("image/") == true) {
            if (defaultPrefs(this).getString(Constants.STUDENT_LOGIN, "false") != "true"
                || !defaultPrefs(this).getBoolean(Constants.ONBOARDING_COMPLETED, false)
            ) {
                viewModel.sendEvent(
                    EventConstants.EVENT_NAME_APP_OPEN_DN,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, EventConstants.IMAGE_SHARE_LOGIN_STUCK)
                    }, true)

                analyticsPublisher.publishEvent(
                    StructuredEvent(category = EventConstants.EVENT_NAME_APP_OPEN_DN_CATEGORY,
                        action = EventConstants.EVENT_NAME_APP_OPEN_DN,
                        eventParams = hashMapOf<String, Any>().apply {
                            put(EventConstants.SOURCE, EventConstants.IMAGE_SHARE_LOGIN_STUCK)
                        })
                )

                openSplashActivity()
            } else {
                val extraStream = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                viewModel.getCropScreenConfig(extraStream)

                Glide.with(this).load(extraStream).into(binding.imageView)

            }
        } else {
            openSplashActivity()
        }
        viewModel.cropConfig.observe(this, EventObserver {
            val cropConfigValue = with(it.first) {
                bundleOf(
                    CropConstant.INTENT_EXTRA_KEY_TITLE to this.cropScreenTitle,
                    CropConstant.INTENT_EXTRA_KEY_FIND_SOLUTION_BUTTON_TEXT to this.findSolutionButtonText
                )
            }
            val imageUri =
                if (it.second == null) Uri.fromFile(File(it.first.sampleImageUri)) else it.second

            CropImage.activity(imageUri).run {
                this@ImageIntentHandlerActivity.let { context ->
                    setBorderLineColor(ContextCompat.getColor(context, R.color.colorAccent))
                    setBorderCornerColor(ContextCompat.getColor(context, R.color.colorAccent))
                    val rect = Rect()
                    binding.seeThroughView.getGlobalVisibleRect(rect)
                    setCropWindow(rect.toRectF())
                    start(this@ImageIntentHandlerActivity, cropConfigValue)
                }
            }
            viewModel.setTrickQuestionShownToTrue()
        })

        binding.buttonContinue.setOnClickListener {
            val extraStream = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            viewModel.getCropScreenConfig(extraStream)
        }

        val countToSendEvent: Int =
            Utils.getCountToSend(RemoteConfigUtils.getEventInfo(), EventConstants.SESSION_START)
        repeat((0 until countToSendEvent).count()) {
            sendEvent(EventConstants.SESSION_START)
        }
    }
}