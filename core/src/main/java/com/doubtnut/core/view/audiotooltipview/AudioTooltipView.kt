package com.doubtnut.core.view.audiotooltipview

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import com.airbnb.lottie.LottieAnimationView
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.DnException
import com.doubtnut.core.R
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.databinding.ViewstubAudioTooltipBinding
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.utils.loadImage2
import com.doubtnut.core.view.ViewActionHandler
import com.doubtnut.core.view.mediaplayer.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class AudioTooltipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ViewActionHandler {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    private val binding: ViewstubAudioTooltipBinding

    private val audioTooltipLifecycleObserver by lazy {
        ViewLifecycleObserver()
    }

    init {
        CoreApplication.INSTANCE.androidInjector().inject(this)
        binding = ViewstubAudioTooltipBinding.inflate(LayoutInflater.from(context), this, true)
        audioTooltipLifecycleObserver.registerActionHandler(this)
    }

    private var audioToolTipView: View? = null

    private val autoPlayAnim: LottieAnimationView? by lazy {
        audioToolTipView?.findViewById<LottieAnimationView>(R.id.autoPlayAnim)
    }
    private val llAudio: ConstraintLayout? by lazy {
        audioToolTipView?.findViewById<ConstraintLayout>(R.id.llAudio)
    }
    private val textViewToolTipArrow: TextView? by lazy {
        audioToolTipView?.findViewById<TextView>(R.id.textViewToolTipArrow)
    }
    private val tvReplayToolTipArrow: TextView? by lazy {
        audioToolTipView?.findViewById<TextView>(R.id.tvReplayToolTipArrow)
    }
    private val layoutTooltip: ConstraintLayout? by lazy {
        audioToolTipView?.findViewById<ConstraintLayout>(R.id.layoutTooltip)
    }
    private val titleToolTipCash: TextView? by lazy {
        audioToolTipView?.findViewById<TextView>(R.id.titleToolTipCash)
    }
    private val ivCloseTooltip: ImageView? by lazy {
        audioToolTipView?.findViewById<ImageView>(R.id.ivCloseTooltip)
    }
    private val ivAudioPlay: ImageView? by lazy {
        audioToolTipView?.findViewById<ImageView>(R.id.ivAudioPlay)
    }

    private lateinit var audioTooltipViewData: AudioTooltipViewData

    private var mediaPlayerStateCallback: IMediaPlayerStateCallback? = null
    private var mediaPlayerVolumeCallback: IMediaPlayerVolumeCallback? = null
    private var mediaPlayerErrorCallback: IMediaPlayerErrorCallback? = null

    private val mediaPlayerVolumeStateListener by lazy {
        object : MediaPlayerManager.MediaPlayerVolumeStateListener {
            override fun onVolumeChange(state: MediaPlayerVolumeState) {
                mediaPlayerVolumeCallback?.onPlayerVolumeChange(
                    state,
                    audioTooltipViewData.screenName
                )
                handlePlayerVolumeState(state)
            }
        }
    }

    private val mediaPlayerErrorListener by lazy {
        object : MediaPlayerManager.MediaPlayerErrorListener {
            override fun onError(errorType: String?, errorCode: String?) {
                mediaPlayerErrorCallback?.onError(errorType, errorCode)
                FirebaseCrashlytics.getInstance()
                    .recordException(DnException("${errorType}_${errorCode})"))
            }
        }
    }

    private val mediaPlayerStateListener by lazy {
        object : MediaPlayerManager.MediaPlayerStateListener {
            override fun onStateChange(state: MediaPlayerState, data: Any?) {
                mediaPlayerStateCallback?.onPlayerStateChange(state, data)
                when (state) {
                    MediaPlayerState.PAUSED, MediaPlayerState.COMPLETED -> {
                        autoPlayAnim?.pauseAnimation()
                        llAudio?.isClickable = true
                        tvReplayToolTipArrow?.visibility = View.VISIBLE
                        layoutTooltip?.visibility = View.VISIBLE
                        textViewToolTipArrow?.visibility = View.INVISIBLE
                        titleToolTipCash?.text = "Tap to Replay"
                    }
                    MediaPlayerState.STARTED -> {
                        autoPlayAnim?.playAnimation()
                        llAudio?.isClickable = true
                    }
                    else -> {}
                }
            }
        }
    }

    private val mediaPlayerManager: AbstractMediaPlayerManager by lazy {
        MediaPlayerFactory.getMediaPlayerManager(
            mediaPlayerVolumeStateListener,
            mediaPlayerStateListener,
            mediaPlayerErrorListener
        )
    }

    fun registerLifecycle(lifecycle: Lifecycle) {
        audioTooltipLifecycleObserver.registerLifecycle(lifecycle)
        mediaPlayerManager.registerLifecycle(lifecycle)
    }

    fun setMediaPlayerStateListener(mediaPlayerStateCallback: IMediaPlayerStateCallback) {
        this.mediaPlayerStateCallback = mediaPlayerStateCallback
    }

    fun registerAudioVolumeListener(mediaPlayerVolumeCallback: IMediaPlayerVolumeCallback) {
        this.mediaPlayerVolumeCallback = mediaPlayerVolumeCallback
    }

    fun registerErrorListener(mediaPlayerErrorCallback: IMediaPlayerErrorCallback) {
        this.mediaPlayerErrorCallback = mediaPlayerErrorCallback
    }

    fun setData(audioTooltipViewData: AudioTooltipViewData) {
        audioTooltipViewData.audioUrl ?: return
        this.audioTooltipViewData = audioTooltipViewData
        setUpAudioTooltipViewStub()
    }

    private fun setUpAudioTooltipViewStub() {
        binding.viewStubAudioTooltip.setOnInflateListener { _, inflated ->
            audioToolTipView = inflated
            setUpAudioTooltipViewData(audioTooltipViewData)
        }
        val viewStub = findViewById<ViewStub>(R.id.view_stub_audio_tooltip)
        if (viewStub == null) {
            audioToolTipView?.visibility = View.VISIBLE
            setUpAudioTooltipViewData(audioTooltipViewData)
        } else {
            if (viewStub.parent != null) {
                viewStub.inflate()
            }
        }
    }

    private fun setUpAudioTooltipViewData(audioTooltipViewData: AudioTooltipViewData) {
        mediaPlayerManager.apply {
            setMedia(audioTooltipViewData.audioUrl!!)
        }

        if (!TextUtils.isEmpty(audioTooltipViewData.tooltipText)) {
            layoutTooltip?.visibility = View.VISIBLE
            textViewToolTipArrow?.visibility = View.VISIBLE
            tvReplayToolTipArrow?.visibility = View.INVISIBLE
            titleToolTipCash?.text = audioTooltipViewData.tooltipText
        } else {
            layoutTooltip?.visibility = View.INVISIBLE
            textViewToolTipArrow?.visibility = View.INVISIBLE
        }

        ivCloseTooltip?.setOnClickListener {
            layoutTooltip?.visibility = View.INVISIBLE
            tvReplayToolTipArrow?.visibility = View.INVISIBLE
            textViewToolTipArrow?.visibility = View.INVISIBLE
        }

        ivAudioPlay?.setOnClickListener {
            mediaPlayerManager.toggleVolume()
        }

        llAudio?.setOnClickListener {
            when (mediaPlayerManager.getMediaPlayerState()) {
                MediaPlayerState.PAUSED, MediaPlayerState.COMPLETED, MediaPlayerState.UNINITIALIZED -> {
                    mediaPlayerManager.playMedia() // replay if completed
                }
                MediaPlayerState.STARTED -> {
                    mediaPlayerManager.pauseMedia() // pause if already playing
                }
                else -> {}
            }
        }
    }

    fun stopMedia() {
        mediaPlayerManager.stopMedia()
        autoPlayAnim?.pauseAnimation()
    }

    // region start - view lifecycle methods
    override fun onPause() {
        super.onPause()
        autoPlayAnim?.pauseAnimation()
    }

    override fun onStop() {
        autoPlayAnim?.clearAnimation()
        super.onStop()
    }
    // region end - view lifecycle methods

    private fun handlePlayerVolumeState(state: MediaPlayerVolumeState) {
        when (state) {
            MediaPlayerVolumeState.MUTED -> {
                sendEvent(
                    analyticsPublisher,
                    CoreEventConstants.AUDIO_MUTED,
                    audioTooltipViewData.screenName.orEmpty()
                )
                ivAudioPlay?.loadImage2(audioTooltipViewData.muteImageUrl)
            }
            MediaPlayerVolumeState.UNMUTED -> {
                sendEvent(
                    analyticsPublisher,
                    CoreEventConstants.AUDIO_PLAYED,
                    audioTooltipViewData.screenName.orEmpty()
                )
                ivAudioPlay?.loadImage2(audioTooltipViewData.unMuteImageUrl)
            }
            else -> {}
        }
    }

    private fun sendEvent(
        analyticsPublisher: IAnalyticsPublisher,
        eventName: String,
        screenName: String
    ) {
        analyticsPublisher.publishEvent(
            CoreAnalyticsEvent(
                eventName, hashMapOf(
                    CoreEventConstants.SCREEN_NAME to screenName,
                )
            )
        )
    }

    interface IMediaPlayerStateCallback {
        fun onPlayerStateChange(state: MediaPlayerState, data: Any?)
    }

    interface IMediaPlayerVolumeCallback {
        fun onPlayerVolumeChange(state: MediaPlayerVolumeState, screenName: String?)
    }

    interface IMediaPlayerErrorCallback {
        fun onError(errorType: String?, errorCode: String?)
    }
}