package com.doubtnutapp.training

import android.graphics.Color
import android.media.MediaPlayer
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import kotlinx.android.synthetic.main.layout_custom_tool_tip.view.*
import tourguide.tourguide.Overlay
import tourguide.tourguide.TourGuide
import javax.inject.Inject

class OnboardingManager(
        val activity: FragmentActivity,
        id: Int,
        title: String,
        description: String,
        buttonText: String,
        listener: (View) -> Unit = {},
        tooltipListener: (View) -> Unit = {},
        customView: CustomToolTip? = null,
        audioUrl: String?,
        isLastScreen: Boolean = false,
        holeRadius: Int = 200,
        colorString: String = "#80808080",
        toolTipWidth: Int = Utils.screenWidth,
        textColorString: String = "#FFFFFF",
        backgroundColorString: String = "#80030303"
) : LifecycleObserver {
    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher


    private var mediaPlayer: MediaPlayer? = null
    private var isAudioPlaying: Boolean = false

    init {
        activity.lifecycle.addObserver(this)
    }

    private val tourGuide = TourGuide.create(activity) {
        overlay {
            setHoleRadius(holeRadius)
            setStyle(Overlay.Style.ROUNDED_RECTANGLE)
            disableClickThroughHole(true)
            backgroundColor = Color.parseColor(colorString)
            setOnClickListener(View.OnClickListener {
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ONBOARDING_CLICK,
                        ignoreFirebase = false,
                        params = hashMapOf(Constants.TOOLTIP_ID to id,
                                Constants.STUDENT_ID to UserUtil.getStudentId(),
                                Constants.POST_PURCHASE_SESSION_COUNT to defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0))))
                releaseMediaPlayer()
                cleanUp()
                listener.invoke(it)
            })
        }
        toolTip {
            setWidth(toolTipWidth)
            if (customView != null) {
                setCustomView(customView)
            } else {
                title { title }
                description { description }
                backgroundColor { Color.parseColor(backgroundColorString) }
                textColor { Color.parseColor(textColorString) }
                setOnClickListener(View.OnClickListener {
                    releaseMediaPlayer()
                    cleanUp()
                    tooltipListener.invoke(it)
                })
            }
        }
    }

    val launchTourGuide = { view: View ->
        tourGuide.playOn(view)
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ONBOARDING_VIEW,
                ignoreFirebase = false,
                params = hashMapOf(Constants.TOOLTIP_ID to id,
                        Constants.STUDENT_ID to UserUtil.getStudentId(),
                        Constants.POST_PURCHASE_SESSION_COUNT to defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0))))
        if (!audioUrl.isNullOrBlank()) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync()
            }
            mediaPlayer?.setOnPreparedListener {
                mediaPlayer?.start()
            }
        }
        activity.lifecycle.addObserver(this)
        val toolTip = tourGuide.toolTip?.getCustomView() as CustomToolTip?
        toolTip?.parentLayout?.maxWidth = Utils.screenWidth
        toolTip?.ivSound?.setOnClickListener {
            if (isAudioPlaying) {
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ONBOARDING_MUTE,
                        ignoreFirebase = false,
                        params = hashMapOf(Constants.TOOLTIP_ID to id,
                                Constants.STUDENT_ID to UserUtil.getStudentId(),
                                Constants.POST_PURCHASE_SESSION_COUNT to defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0))))
                isAudioPlaying = false
                toolTip.ivSound.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_icon_small_volume_mute))
                mediaPlayer?.setVolume(0f, 0f)
            } else {
                isAudioPlaying = true
                toolTip.ivSound.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_speaker))
                mediaPlayer?.setVolume(1.00f, 1.0f)
            }
        }
        toolTip?.setBackgroundColor(Utils.parseColor(backgroundColorString))
        toolTip?.title?.text = title
        toolTip?.description?.text = description
        toolTip?.ivNext?.text = buttonText
        toolTip?.ivNext?.setOnClickListener {
            releaseMediaPlayer()
            analyticsPublisher.publishEvent(AnalyticsEvent( EventConstants.ONBOARDING_CLICK,
                    ignoreFirebase = false,
                    params = hashMapOf(Constants.TOOLTIP_ID to id,
                            Constants.STUDENT_ID to UserUtil.getStudentId(),
                            Constants.POST_PURCHASE_SESSION_COUNT to defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0))))
            if (!isLastScreen) {
                tourGuide.cleanUp()
                tooltipListener.invoke(it)
            } else {
                deeplinkAction.performAction(activity, "doubtnutapp://home")
            }
        }
        toolTip?.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ONBOARDING_CLICK,
                    ignoreFirebase = false,
                    params = hashMapOf(Constants.TOOLTIP_ID to id,
                            Constants.STUDENT_ID to UserUtil.getStudentId(),
                            Constants.POST_PURCHASE_SESSION_COUNT to defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0))))
            releaseMediaPlayer()
            tourGuide.cleanUp()
            tooltipListener.invoke(it)
        }
        toolTip?.parentLayout?.setBackgroundColor(Utils.parseColor(backgroundColorString))
    }
    val dismissTourGuide = {
        tourGuide.cleanUp()
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        dismissTourGuide()
    }
}