package com.doubtnutapp.video

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.provider.Settings
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.airbnb.lottie.LottieDrawable
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.DialogDateChangeBinding
import com.doubtnutapp.databinding.FragmentVideoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.videoPage.entities.AnalysisData
import com.doubtnutapp.domain.videoPage.entities.EventDetails
import com.doubtnutapp.doubletapplayerview.youtube.YouTubeOverlay
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.ImaAdData
import com.doubtnutapp.ui.mediahelper.ImaAdEventListener
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_HLS
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.video.videoquality.OnQualitySelectionListener
import com.doubtnutapp.video.videoquality.VideoQualityBottomSheet
import com.doubtnutapp.videoPage.model.AdResource
import com.doubtnutapp.videoPage.model.ImaAdTagResourceData
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.videoscreen.YoutubeFragment
import com.doubtnutapp.videoscreen.YoutubeFragmentListener
import com.doubtnutapp.widgets.ExoSpeedView
import com.doubtnutapp.youtubeVideoPage.FullScreenHelper
import com.facebook.appevents.AppEventsConstants
import com.google.android.exoplayer2.DefaultControlDispatcher
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.ResizeMode
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.TimeBar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.parcel.Parcelize
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/*
 Created by Naman on 04/03/2020
*/

/*
 Reusable video fragment that supports exoplayer and youtube.
 - Wraps complete exoplayer functionality
 - And thinly wraps Youtube player (YoutubeFragment is the complete wrapper around Youtube player)
*/

class VideoFragment : BaseBindingFragment<VideoFragmentViewModel, FragmentVideoBinding>(),
    ExoPlayerHelper.ExoPlayerStateListener,
    ExoPlayerHelper.VideoEngagementStatusListener,
    ExoPlayerHelper.AdVideoEngagementStatusListener,
    ExoPlayerHelper.MediaSourceStatusListener,
    ExoPlayerHelper.FallbackElapsedTimeListener,
    ExoPlayerHelper.PositionDiscontinuityListener,
    ExoPlayerHelper.AdProgressUpdateLister,
    ExoPlayerHelper.ProgressListener,
    YoutubeFragmentListener, View.OnTouchListener {

    companion object {

        private const val TAG = "YoutubeFragment"

        private const val VIDEO_DATA = "video_data"
        private const val EVENT_MAP = "event_map"
        private const val YOUTUBE_VIDEO_DATA = "youtube_video_data"
        const val DEFAULT_ASPECT_RATIO = "16:9"

        private const val BOTTOM_MARGIN_LANDSCAPE = 80
        private const val BOTTOM_MARGIN_PORTRAIT = 15
        private const val BOTTOM_MARGIN_NCERT = 110

        fun newInstance(
            videoData: VideoData,
            eventDetail: EventDetails?,
            videoFragmentListener: VideoFragmentListener,
            playFailedListener: PlayFailedListener? = null,
            switchToTimeShiftListener: SwitchToTimeShiftListener? = null,
            goLiveListener: GoLiveListener? = null,
            @ResizeMode resizeMode: Int? = null
        ) =
            VideoFragment().apply {
                this.videoFragmentListener = videoFragmentListener
                this.playerFailedListener = playFailedListener
                this.switchToTimeShiftListener = switchToTimeShiftListener
                this.goLiveListener = goLiveListener
                this.resizeMode = resizeMode
                arguments = Bundle().apply {
                    putParcelable(VIDEO_DATA, videoData)
                    putParcelable(EVENT_MAP, eventDetail)
                }
            }

        fun newYoutubeInstance(
            videoData: YoutubeVideoData,
            videoFragmentListener: VideoFragmentListener,
            playFailedListener: PlayFailedListener? = null
        ) =
            VideoFragment()
                .apply {
                    this.videoFragmentListener = videoFragmentListener
                    this.playerFailedListener = playFailedListener
                    arguments = Bundle().apply {
                        putParcelable(YOUTUBE_VIDEO_DATA, videoData)
                    }
                }

        @Parcelize
        data class VideoData(
            val questionId: String,
            val videoUrl: String,
            /*
             sometimes hls videos don't play and we provide a normal fallback video
             url. In some cases, we don't require hls at all, in such cases we set
             hls timeout to 0 and set both video and fallbackurl to the non hls video url
             */
            val fallbackVideoUrl: String = videoUrl,
            val hlsTimeoutTime: Long = 0,
            val aspectRatio: String = DEFAULT_ASPECT_RATIO,
            val autoPlay: Boolean = true,
            val show_fullscreen: Boolean = true,
            /*
            the position from where to start the player
            */
            val startPosition: Long = 0,
            /*
             the page from where user comes on to the video
            */
            val page: String = "",
            /*
              used to track if the video was played earlier already and then user
              moved to a new video and then came back, we use this info to track
              certain video views info
            */
            val isPlayFromBackStack: Boolean = false,
            /*
              used to track video views. If we are already tracking video view from
              somewhere else, then pass the corresponding vewId and future views will
              be tracked with the same viewId
              Note - do not use viewId when fragment is in a recyclerview
              If view id is not present, then a new viewId is fetched and used
              See viewModel.publishVideoViewOnboarding
             */
            val viewId: String = "",
            val mediaType: String? = null,
            val drmScheme: String? = null,
            val drmLicenseUrl: String? = null,
            val useFallBack: Boolean = true,
            val addDummyProgress: Boolean = false,
            val showGoLiveText: Boolean = false,
            val playBackUrlList: List<VideoResource.PlayBackData>? = null,
            val videoStartTime: Long? = null,
            val overrideMaxSeekTime: Boolean = false,
            val hideProgressDuration: Boolean = false,
            val controllerAutoShow: Boolean = true,
            val showEnterPipModeButton: Boolean = false,
            val adResource: AdResource? = null,
            val isInPipMode: Boolean = false,
            val blockForwarding: Boolean = false,
            val ncertExperiment: Boolean? = false,
            val answerId: String? = null,
            val expertId: String? = null,
            val videoName: String? = null,
            val isPremium: Boolean? = false,
            val isVip: Boolean? = false,
            val chapter: String? = null,
            val subject: String? = null,
            val videoLanguage: String? = null,
            val bottomView: String? = null,
            val fromViewHolder: Boolean? = false,
            val isLive: Boolean? = false,
            val typeOfContent: String? = null,
            val ocrText: String? = null,
            val lockUnlockLogs: String?,
            var analysisData: AnalysisData? = null,
            val imaAdTagResourceData: List<ImaAdTagResourceData>? = null
        ) : Parcelable

        @Parcelize
        data class YoutubeVideoData(
            val youtubeId: String,
            val autoPlay: Boolean = false,
            val showFullScreen: Boolean = true,
            val showEngagementTime: Boolean = true,
            val viewId: String = "",
            val startSeconds: Float = 0f
        ) : Parcelable
    }

    var exoPlayerHelper: ExoPlayerHelper? = null
    private var youtubeFragment: YoutubeFragment? = null

    private var isYoutube: Boolean = false
    private var questionId: String? = null
    private var videoData: VideoData? = null
    private var eventDetail: EventDetails? = null

    private var isStarted = false

    private var isVisualizerShowing = false

    private lateinit var videoFragmentListener: VideoFragmentListener
    private var playerFailedListener: PlayFailedListener? = null
    private var switchToTimeShiftListener: SwitchToTimeShiftListener? = null
    private var goLiveListener: GoLiveListener? = null

    private var initCompleted: Boolean = false
    private var videoViewHeight: Int = -1

    private var page: String = ""
    private var viewId: String = ""

    private var hlsTimeoutTime: Long = 0
    private var skipApDuration = 0L
    private var adDeepLink: String? = null
    private var adPosition: ExoPlayerHelper.AdPosition = ExoPlayerHelper.AdPosition.START

    private var fullScreenHelper: FullScreenHelper? = null

    @ResizeMode
    private var resizeMode: Int? = null

    var isFullScreen = false

    var gestureDetector: GestureDetector? = null

    var timeAnalytics = ArrayDeque<TimeAnalytics>()

    var lastPosition: Long? = null

    private var isAdStarted = false

    private val mPipModeEnterButton: ImageView? by lazy {
        mBinding?.playerView?.findViewById<ImageView>(
            R.id.exo_enter_pip
        )
    }
    private var videoQualityBottomSheet: VideoQualityBottomSheet? = null

    @Inject
    lateinit var disposable: CompositeDisposable

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    /*
        @Inject
        lateinit var convivaVideoAnalytics: Lazy<ConvivaVideoAnalytics>
    */
    @Inject
    lateinit var networkUtil: NetworkUtil

    enum class PlayerState {
        INIT, PLAY, PAUSE, BUFFER, END
    }

    data class TimeAnalytics(
        val state: PlayerState,
        var startTime: Long,
        var endTime: Long?
    )

    private var sourceType: String? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        activity?.let {
            fullScreenHelper = FullScreenHelper(it)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // if activity is recreated (e.g app goes in background and then user comes back after long time),
        // the videoFragmentListener registered in newInstance wouldn't be there, we try to manually
        // register it back if activity has it implemented
        if (!::videoFragmentListener.isInitialized) {
            if (activity is VideoFragmentListener) {
                this.videoFragmentListener = requireActivity() as VideoFragmentListener
            } else {
                videoFragmentListener = object : VideoFragmentListener {}
            }
        }

        addSeekPositionObserver()
        addTimeBarScrubListener()
        lastPosition = null

        viewModel = viewModelFactory.create(VideoFragmentViewModel::class.java)

        // using observerForver as we need viewId to be published even after it has been set back into
        // CREATED state.
        viewModel.getViewIdLiveData.observeForever(object : Observer<String> {
            override fun onChanged(it: String?) {
                it?.let { viewId ->
                    this@VideoFragment.viewId = viewId
                    this@VideoFragment.videoFragmentListener.onViewIdPublished(viewId)
                    viewModel.getViewIdLiveData.removeObserver(this)
                }
            }
        })

        if (requireArguments().getParcelable<YoutubeVideoData>(YOUTUBE_VIDEO_DATA) != null) {
            val youtubeVideoData =
                requireArguments().getParcelable<YoutubeVideoData>(YOUTUBE_VIDEO_DATA)!!
            showYoutubeView(youtubeVideoData)
        } else if (requireArguments().getParcelable<VideoData>(VIDEO_DATA) != null) {
            showExoView(requireArguments().getParcelable<VideoData>(VIDEO_DATA))
        } else {
            return
        }
    }

    private fun showYoutubeView(youtubeVideoData: YoutubeVideoData) {
        // youtube player setup
        isYoutube = true
        setFullScreenStatus()
        mBinding?.youtubeFragmentVideoPage?.show()
        mBinding?.videoContainer?.hide()
        this.viewId = youtubeVideoData.viewId
        childFragmentManager.beginTransaction().apply {
            youtubeFragment = YoutubeFragment.newInstance(
                youtubeVideoData.youtubeId,
                youtubeVideoData.startSeconds,
                videoFragmentListener,
                this@VideoFragment,
                youtubeVideoData.showEngagementTime,
                youtubeVideoData.showFullScreen
            ).also {
                add(R.id.youtubeFragmentVideoPage, it)
            }
            commit()
        }
        updateAspectRatioOfPlayer()
        setUpLandscapeSimilarFragment()
        videoFragmentListener.removeLandscapeSimilarFragment()
    }

    private fun showExoView(videoData: VideoData?) {
        isYoutube = false
        initVideo(videoData)
        removeYoutubeView()
        mBinding?.youtubeFragmentVideoPage?.hide()
        mBinding?.videoContainer?.show()
        setPlayerControllerVisibilityListener()
        this.videoData = videoData
        this.eventDetail = requireArguments().getParcelable<EventDetails>(EVENT_MAP)
        this.questionId = this.videoData!!.questionId
        this.page = this.videoData!!.page
        this.viewId = this.videoData!!.viewId

        updateAspectRatioOfPlayer()
        adjustBottomMargin(if (videoData!!.ncertExperiment == true && activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) BOTTOM_MARGIN_NCERT else null)
        setUpLandscapeSimilarFragment()
        isVisualizerShowing = Utils.hasAudioExtension(videoData.videoUrl)
        setupExoPlayer()
    }

    private var isConvivaSessionStarted = false

    private fun setUpConvivaAnalytics() {
        /*
        this.videoData?.let { videoData ->
            // Pass content info to Conviva
            val contentInfo = ConvivaHelper.getContentInfo(
                videoUrl = videoData.videoUrl,
                assetName = this.questionId!! + videoData.ocrText?.take(20)?.prependIndent("-"),
                isLive = videoData.isLive == true,
                encodedFrameRate = 0
            ) +
                    ConvivaHelper.getVideoContentMetaData(
                        contentType = if (videoData.isLive == true) "LIVE" else "VOD",
                        assetId = this.questionId!!,

                        ) +
                    ConvivaHelper.getCustomTags(
                        subject = videoData.subject,
                        chapter = videoData.chapter,
                        subscription = videoData.isPremium == true,
                        videoFormat = videoData.mediaType,
                        medium = videoData.videoLanguage,
                        videoUrl = videoData.videoUrl,
                        typeOfContent = videoData.analysisData?.typeOfContent,
                        facultyId = videoData.analysisData?.facultyId
                            ?: videoData.expertId.toString(),
                        facultyName = videoData.analysisData?.facultyName,
                        assortmentId = videoData.analysisData?.assortmentId.toString(),
                        batchId = videoData.analysisData?.batchId.toString(),
                        subscriptionStart = videoData.analysisData?.subscriptionStart,
                        subscriptionEnd = videoData.analysisData?.subscriptionEnd,
                        isVip = videoData.analysisData?.isVip,
                        courseId = videoData.analysisData?.courseId,
                        courseTitle = videoData.analysisData?.courseTitle
                    ) +
                    hashMapOf<String, Any>().apply {
                        put(Constants.NETWORK_STATE, networkUtil.isConnectionFast())
                    }
            convivaVideoAnalytics.get().reportPlaybackRequested(contentInfo)
            isConvivaSessionStarted = true
        }
        */
    }

    private fun updateAspectRatioOfPlayer() {
        if (isYoutube) {
            updateConstraintDimensionRatio(DEFAULT_ASPECT_RATIO)
        } else {
            if (activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                updateConstraintDimensionRatio(calculateAspectRatio())
            } else {
                updateConstraintDimensionRatio(videoData!!.aspectRatio)
            }
        }

    }

    private fun setUpLandscapeSimilarFragment() {
        if (!isYoutube) {
            if (activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                videoFragmentListener.addLandscapeSimilarFragment()
            } else {
                videoFragmentListener.removeLandscapeSimilarFragment()
            }
        } else {
            videoFragmentListener.removeLandscapeSimilarFragment()
        }
    }

    private fun calculateAspectRatio(): String {
        val metrics = requireContext().resources.displayMetrics
        val ratio = metrics.widthPixels.toFloat() / metrics.heightPixels.toFloat()
        return ratio.toString()
    }

    private fun initVideo(videoData: VideoData? = null) {
        exoPlayerHelper = ExoPlayerHelper(requireActivity(), mBinding?.playerView!!)
        exoPlayerHelper?.setOverlayView(mBinding?.youtubeOverlay!!)
        if (videoData?.controllerAutoShow == false) {
            mBinding?.playerView?.controllerAutoShow = false
        } else {
            mBinding?.playerView?.showController()
            mBinding?.playerView?.controllerAutoShow = true
        }
        if (resizeMode != null) {
            mBinding?.playerView?.resizeMode = this.resizeMode!!
        }
        lifecycle.addObserver(exoPlayerHelper!!)
        initVideoRotation()

        val fastForwardIncMs = if (videoData?.blockForwarding == true) {
            0L
        } else {
            10000L
        }

        mBinding?.playerView?.setControlDispatcher(object :
            DefaultControlDispatcher(fastForwardIncMs, 10000) {
            private var maxPlayedPositionMs: Long = 0
            override fun dispatchSetPlayWhenReady(player: Player, playWhenReady: Boolean): Boolean {
                val result = super.dispatchSetPlayWhenReady(player, playWhenReady)
                if (playWhenReady.not()) {
                    sendUserPlayerPauseEvent()
                }
                return result
            }

            override fun dispatchSeekTo(
                player: Player,
                windowIndex: Int,
                positionMs: Long
            ): Boolean {
                return if (videoData?.blockForwarding == true) {
                    maxPlayedPositionMs = maxPlayedPositionMs.coerceAtLeast(player.currentPosition)
                    player.seekTo(windowIndex, positionMs.coerceAtMost(maxPlayedPositionMs))
                    return true
                } else {
                    super.dispatchSeekTo(player, windowIndex, positionMs)
                }
            }
        })

        initCompleted = true
    }

    private fun setAdView() {
        videoData?.adResource?.let {
            if (!it.adCtaText.isNullOrEmpty()) {
                mBinding?.adImageView?.setVisibleState(false)
                mBinding?.btnAdLink?.setBackgroundColor(Color.parseColor(it.adButtonColor))
                mBinding?.btnAdLink?.text = it.adButtonText
                mBinding?.adDescView?.setBackgroundColor(Color.parseColor(it.adCtaBgColor))
                mBinding?.tvAdDesc?.text = it.adCtaText
                mBinding?.tvAdDesc?.setTextColor(Color.parseColor(it.adCtaTextColor))
            } else {
                mBinding?.btnAdLink?.setVisibleState(false)
                mBinding?.tvAdDesc?.setVisibleState(false)
                mBinding?.adImageView?.setVisibleState(true)
                mBinding?.adDescView?.setPadding(0, 0, 0, 0)
                mBinding?.adImageView?.loadImageEtx(it.adImageUrl.orEmpty())
                mBinding?.adImageView?.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.AD_BANNER_CLICK,
                            hashMapOf(
                                EventConstants.DURATION to exoPlayerHelper?.getAdCurrentPosition()
                                    .toString(),
                                EventConstants.SOURCE_ID to videoData?.adResource?.adUrl.orEmpty(),
                                EventConstants.AD_ID to videoData?.adResource?.adId.orEmpty(),
                                EventConstants.PAGE to videoData?.page.orEmpty(),
                                EventConstants.VIDEO_VIEW_ID to videoData?.viewId.orEmpty(),
                                EventConstants.QUESTION_ID to videoData?.questionId.orEmpty()
                            )
                        )
                    )
                    deeplinkAction.performAction(requireContext(), adDeepLink)
                }
            }
        }
    }

    private fun setupExoPlayer() {
        timeAnalytics.clear()
        updatePlayerAnalytics(PlayerState.INIT)
        exoPlayerHelper!!.setPipMode(videoData?.isInPipMode ?: false)
        exoPlayerHelper!!.setMediaData(
            videoData?.mediaType.orDefaultValue(MEDIA_TYPE_HLS),
            videoData?.drmScheme, videoData?.drmLicenseUrl,
            videoData!!.useFallBack, videoData?.videoStartTime,
            videoData?.overrideMaxSeekTime ?: false
        )
        exoPlayerHelper!!.setVideoUrl(videoData!!.videoUrl)

        // IMA ad tag
        val imaAdTagData = videoData?.imaAdTagResourceData.orEmpty()
        val imaAdTag = if (imaAdTagData.isNotEmpty()) {
            imaAdTagData[0].adTag.orEmpty()
        } else {
            ""
        }
        val imaAdTimeout = if (imaAdTagData.isNotEmpty()) {
            imaAdTagData[0].adTimeout ?: 0
        } else {
            0
        }
        if (imaAdTag.isNotEmpty()) {
            exoPlayerHelper!!.setImaAdTagUrl(imaAdTag)
        }
        if (imaAdTimeout > 0) {
            exoPlayerHelper!!.setImaAdMediaLoadTimeout(imaAdTimeout)
        }

        exoPlayerHelper!!.setImaAdEventListener(object : ImaAdEventListener {
            override fun onStarted(adData: ImaAdData?) {
                super.onStarted(adData)
                sendImaAdEvent(CoreEventConstants.IMA_AD_STARTED, adData)
            }

            override fun onPaused(adData: ImaAdData?) {
                super.onPaused(adData)
                sendImaAdEvent(CoreEventConstants.IMA_AD_PAUSED, adData)
            }

            override fun onResumed(adData: ImaAdData?) {
                super.onResumed(adData)
                sendImaAdEvent(CoreEventConstants.IMA_AD_RESUMED, adData)
            }

            override fun onCompleted(adData: ImaAdData?) {
                super.onCompleted(adData)
                sendImaAdEvent(CoreEventConstants.IMA_AD_COMPLETED, adData)
            }

            override fun onSkipped(adData: ImaAdData?) {
                super.onSkipped(adData)
                sendImaAdEvent(CoreEventConstants.IMA_AD_SKIPPED, adData)
            }

            override fun onAdTapped(adData: ImaAdData?) {
                super.onAdTapped(adData)
                sendImaAdEvent(CoreEventConstants.IMA_AD_TAPPED, adData)
            }

            override fun onError(adData: ImaAdData?) {
                super.onError(adData)
                sendImaAdEvent(CoreEventConstants.IMA_AD_ERROR, adData)
            }
        })

        exoPlayerHelper!!.sendVideoDetails(
            id = videoData!!.questionId,
            name = videoData!!.ocrText ?: "",
            content = videoData!!.typeOfContent ?: ""
        )

        videoData?.adResource?.let {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.AD_START,
                    hashMapOf(
                        EventConstants.SOURCE_ID to it.adUrl,
                        EventConstants.AD_ID to videoData?.adResource?.adId.orEmpty(),
                        EventConstants.PAGE to videoData?.page.orEmpty(),
                        EventConstants.VIDEO_VIEW_ID to videoData?.viewId.orEmpty(),
                        EventConstants.QUESTION_ID to videoData?.questionId.orEmpty()
                    )
                )
            )
            exoPlayerHelper!!.setAdUrl(it.adUrl)
            adDeepLink = it.adButtonDeepLink
            adPosition = it.adPosition
            skipApDuration = it.adSkipDuration
            exoPlayerHelper!!.setAdContentStartPosition(it.midAdStartDuration)
            exoPlayerHelper!!.setAdPosition(it.adPosition)
        }
        exoPlayerHelper!!.setFallbackUrl(videoData!!.fallbackVideoUrl)
        exoPlayerHelper!!.setHlsTimeoutTime(videoData!!.hlsTimeoutTime)
        exoPlayerHelper!!.setPlayerCurrentPosition(videoData!!.startPosition)
        val progressViewLive =
            mBinding?.playerView?.findViewById<DefaultTimeBar>(R.id.exo_live_progress)
        val progressView = mBinding?.playerView?.findViewById<DefaultTimeBar>(R.id.exo_progress)
        if (!videoData!!.addDummyProgress) {
            if (videoData!!.autoPlay) {
                exoPlayerHelper!!.startPlayingVideo()
            }
            progressViewLive?.removeListener(dummyScrubListener)
            progressView?.show()
        } else {
            progressView?.hide()
            progressViewLive?.show()
            progressViewLive?.setDuration(100)
            progressViewLive?.setPosition(100)
            progressViewLive?.addListener(dummyScrubListener)
            progressViewLive?.setPlayedColor(resources.getColor(R.color.tomato))
            hidePlayerController()
        }

        if (!videoData!!.show_fullscreen) {
            mBinding?.playerView?.findViewById<ImageView>(R.id.exo_fullscreen)?.visibility =
                View.GONE
        }

        if (videoData!!.showGoLiveText) {
            val goLiveView = mBinding?.playerView?.findViewById<TextView>(R.id.exo_go_live)
            goLiveView?.visibility = View.VISIBLE
            goLiveView?.setOnClickListener {
                goLiveListener?.invoke()
            }
        } else {
            mBinding?.playerView?.findViewById<TextView>(R.id.exo_go_live)?.visibility = View.GONE
        }

        val exoPosition = mBinding?.playerView?.findViewById<TextView>(R.id.exo_position)
        val exoDuration = mBinding?.playerView?.findViewById<TextView>(R.id.exo_duration)

        if (videoData!!.hideProgressDuration) {
            exoPosition?.visibility = View.GONE
            exoDuration?.visibility = View.GONE
        } else {
            exoPosition?.visibility = View.VISIBLE
            exoDuration?.visibility = View.VISIBLE
        }

        val exoTrackSelectionView =
            mBinding?.playerView?.findViewById<ImageView>(R.id.exo_track_selection)

        if (videoData!!.playBackUrlList.isNullOrEmpty()) {
            exoTrackSelectionView?.hide()
        } else {
            exoTrackSelectionView?.show()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    CoreEventConstants.EXOPLAYER_SETTINGS_ICON_VISIBLE,
                    hashMapOf(
                        CoreEventConstants.QUESTION_ID to questionId.toString(),
                        CoreEventConstants.PAGE to page
                    )
                )
            )
        }

        exoTrackSelectionView?.setOnClickListener {
            if (videoData!!.playBackUrlList.isNullOrEmpty().not()) {
                showVideoQualityBottomSheet(
                    videoData!!.playBackUrlList!!,
                    videoData!!.mediaType.orEmpty(),
                    videoData!!.useFallBack
                )
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        CoreEventConstants.EXOPLAYER_SETTINGS_ICON_CLICKED,
                        hashMapOf(
                            CoreEventConstants.QUESTION_ID to questionId.toString(),
                            CoreEventConstants.PAGE to page
                        )
                    )
                )
            }

        }
        setAdView()
    }

    private fun sendImaAdEvent(eventName: String, imaAdData: ImaAdData?) {
        viewModel.publishImaAdEvent(
            eventName = eventName,
            viewId = videoData?.viewId,
            questionId = videoData?.questionId,
            adData = imaAdData
        )
    }

    fun setExoCurrentPosition(position: Long) {
        exoPlayerHelper?.seekTo(position)
        exoPlayerHelper?.resumePlay()
    }

    private fun showVideoQualityBottomSheet(
        playBackUrlList: List<VideoResource.PlayBackData>,
        mediaType: String, useFallBack: Boolean
    ) {
        VideoQualityBottomSheet.newInstance(ArrayList(playBackUrlList))
            .apply {
                videoQualityBottomSheet = this
                setItemClickListener(object : OnQualitySelectionListener {
                    override fun onQualityChanged(playbackData: VideoResource.PlayBackData) {
                        dismiss()
                        val key = playbackData.resource
                        val drmLicenseUrl = playbackData.drmLicenseUrl
                        val drmScheme = playbackData.drmScheme
                        if (key.isNotBlank()) {
                            exoPlayerHelper?.onTrackSelection(
                                mediaSource = mediaType,
                                videoUrl = key,
                                drmScheme = drmScheme,
                                drmLicenseUrl = drmLicenseUrl,
                                useFallback = useFallBack
                            )
                        }

                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                CoreEventConstants.VIDEO_QUALITY_BOTTOM_SHEET,
                                hashMapOf(
                                    CoreEventConstants.CLICKED to playbackData.display.toString(),
                                    CoreEventConstants.QUESTION_ID to questionId.toString(),
                                    CoreEventConstants.PAGE to page
                                )
                            )
                        )

                    }
                })
            }.show(childFragmentManager, VideoQualityBottomSheet.TAG)
    }

    /*
    * to handle default track selector, currently we are not supporting default selector
    */
    private fun showTrackSelectionDialog() {
        exoPlayerHelper?.showTrackSelectionDialog(childFragmentManager)
    }

    private val dummyScrubListener = object : TimeBar.OnScrubListener {
        override fun onScrubMove(timeBar: TimeBar, position: Long) {

        }

        override fun onScrubStart(timeBar: TimeBar, position: Long) {

        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            switchToTimeShiftListener?.invoke(position)
        }
    }

    fun updateVideoData(videoData: VideoData) {
        this.videoData = videoData
        resetVideo()
    }

    fun updateConstraintDimensionRatio(dimensionRatio: String) {
        setUpConstraintDimension(mBinding?.videoContainer!!, dimensionRatio)
    }

    fun startPlaying() {
        if (!initCompleted) return
        exoPlayerHelper?.startPlayingVideo()

    }

    fun updateData(videoData: VideoData) {
        arguments = Bundle().apply {
            putParcelable(VIDEO_DATA, videoData)
        }
        this.videoData = videoData
        this.questionId = this.videoData!!.questionId
        this.page = this.videoData!!.page
        this.viewId = this.videoData!!.viewId
    }

    fun resetVideo() {
        if (!initCompleted) return
        closeConvivaSession()
        if (isYoutube) {
            removeYoutubeView()
        } else {
            exoPlayerHelper?.videoChanged()
        }
    }

    fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus && isFullScreen) {
            fullScreenHelper?.enterFullScreen()
        }
    }

    fun enterFullscreen() {
        isFullScreen = true
        if (!initCompleted) return
        if (isYoutube) {
            mBinding?.youtubeFragmentVideoPage?.layoutParams?.height = getScreenWidth()
            fullScreenHelper?.enterFullScreen()
        } else {
            mBinding?.videoContainer?.layoutParams?.height = getScreenWidth()
            fullScreenHelper?.enterFullScreen()
        }
        adjustBottomMargin(if (videoData!!.ncertExperiment == true) BOTTOM_MARGIN_NCERT else null)
        mBinding?.adDescView?.hide()
    }

    fun exitFullscreen() {
        isFullScreen = false
        if (!initCompleted) return
        if (isYoutube) {
            mBinding?.youtubeFragmentVideoPage?.layoutParams?.height = getScreenWidth() / 3
        } else {
            mBinding?.videoContainer?.layoutParams?.height = getHeight()
            fullScreenHelper?.exitFullScreen()
        }
        adjustBottomMargin()
        if (!adDeepLink.isNullOrEmpty() && isAdStarted) {
            mBinding?.adDescView?.show()
        } else {
            mBinding?.adDescView?.hide()
        }
    }

    // Return current position of player in seconds
    fun getCurrentPosition(): Int =
        if (isYoutube) {
            youtubeFragment?.getCurrentSecond()?.toInt() ?: 0
        } else {
            (exoPlayerHelper?.getPlayerCurrentPosition()?.div(1000))?.toInt() ?: 0
        }

    // Return duration of video in seconds
    fun getVideoDuration(): Int =
        if (isYoutube) {
            youtubeFragment?.getVideoDuration()?.toInt() ?: 0
        } else {
            exoPlayerHelper?.getDuration()?.toInt() ?: 0
        }

    // Returns engagement time in millis, without pausing the engagement time tracking
    fun getCurrentEngagementTime(): Int =
        TimeUnit.MILLISECONDS.toSeconds(
            if (isYoutube) {
                youtubeFragment?.getCurrentEngagementTime() ?: 0
            } else {
                exoPlayerHelper?.getCurrentEngagementTime() ?: 0
            }
        ).toInt()

    private fun getHeight(): Int {
        val aspectRatio = videoData?.aspectRatio ?: DEFAULT_ASPECT_RATIO
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        var value = screenWidth * 9 / 16
        try {
            val width: Int? =
                aspectRatio.replace("\"", "").replaceAfter(":", "").replace(":", "").toIntOrNull()
            val height: Int? =
                aspectRatio.replace("\"", "").replaceBefore(":", "").replace(":", "").toIntOrNull()
            if (width != null && height != null) {
                value = screenWidth * height / width
            }
        } catch (e: Exception) {
            value = screenWidth * 9 / 16
        }
        return value
    }

    fun adjustBottomMargin(bottomMargin: Int? = null) {
        if (isYoutube) {
            if (activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mBinding?.youtubeFragmentVideoPage?.setMargins(
                    0, 0, 0, bottomMargin
                        ?: BOTTOM_MARGIN_LANDSCAPE
                )
            } else {
                mBinding?.youtubeFragmentVideoPage?.setMargins(
                    0, 0, 0, bottomMargin
                        ?: BOTTOM_MARGIN_PORTRAIT
                )
            }
        } else {
            if (mBinding?.playerView == null) return
            val bottomLayout = mBinding?.playerView!!.findViewById<LinearLayout>(R.id.linearLayout3)
            if (activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                bottomLayout.setPadding(0, 0, 0, bottomMargin ?: BOTTOM_MARGIN_LANDSCAPE)
            } else {
                bottomLayout.setPadding(0, 0, 0, bottomMargin ?: BOTTOM_MARGIN_PORTRAIT)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isYoutube) {
            initializePlayer()
            if (viewId.isEmpty()) {
                viewModel.publishVideoViewOnboarding(questionId!!, "0", "0", page, getStudentId())
            }
        }
        handleVideoCounterEvent()
    }

    private fun handleVideoCounterEvent() {

        val counter = defaultPrefs()
            .getInt(Constants.USER_VIDEO_WATCH_COUNTER, 0) + 1

        defaultPrefs().edit {
            putInt(Constants.USER_VIDEO_WATCH_COUNTER, counter)
        }
        if (counter == 1) {
            viewModel.publishEventWith(AppEventsConstants.EVENT_NAME_ACHIEVED_LEVEL, hashMapOf())
        } else if (counter == 7) {
            viewModel.publishEventWith(
                AppEventsConstants.EVENT_NAME_UNLOCKED_ACHIEVEMENT,
                hashMapOf()
            )
        }

        val installationDate = Date(
            context?.packageManager?.getPackageInfo(
                context?.packageName.orEmpty(),
                0
            )?.firstInstallTime!!
        )

        if (DateUtils.isToday(installationDate.time)) {
            viewModel.publishEventWith(
                AppEventsConstants.EVENT_NAME_ADDED_TO_WISHLIST,
                hashMapOf(),
                true
            )
        } else if (isYesterday(installationDate)) {
            viewModel.publishEventWith(
                AppEventsConstants.EVENT_NAME_INITIATED_CHECKOUT,
                hashMapOf(),
                true
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isYoutube) {
            if (!initCompleted) return
            exoPlayerHelper?.setAudioAttributes(viewModel.getAudioAttributes(), false)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!timeAnalytics.isEmpty()) {
            updatePlayerAnalytics(PlayerState.END)
            calculateVideoAnalytics()
        }
        mBinding?.progressBarExoPlayerBuffering?.hide()
    }

    override fun onStop() {
        if (!isYoutube) {
            if (!initCompleted) return
            exoPlayerHelper?.unSetListeners()
            if (videoData?.fromViewHolder == true) {
                closeConvivaSession()
            }
        }
        super.onStop()
    }

    private fun initializePlayer() {

        exoPlayerHelper!!.setExoPlayerStateListener(this)
        exoPlayerHelper!!.setMediaSourceStatusListener(this)
        exoPlayerHelper!!.setVideoEngagementStatusListener(this)
        exoPlayerHelper!!.setAdVideoEngagementStatusListener(this)
        exoPlayerHelper!!.setFallbackElapsedTimeListener(this)
        exoPlayerHelper!!.setPositionDiscontinuityListener(this)
        exoPlayerHelper!!.setAdProgressUpdateListener(this)
        exoPlayerHelper!!.setProgressListener(1000, this)

        mBinding?.playerView?.findViewById<ImageView>(R.id.exo_fullscreen)?.setOnClickListener {
            if (!isFullScreen) {
                isFullScreen = true

                sendClevertapEvent(EventConstants.EVENT_NAME_VIDEO_FULL_SCREEN_BTN)
                sendEvent(EventConstants.EVENT_NAME_VIDEO_FULL_SCREEN_BTN)

                videoFragmentListener.onFullscreenRequested()
            } else {
                isFullScreen = false

                sendClevertapEvent(EventConstants.EVENT_NAME_VIDEO_EXIT_FULL_SCREEN_BTN)
                sendEvent(EventConstants.EVENT_NAME_VIDEO_EXIT_FULL_SCREEN_BTN)

                videoFragmentListener.onPortraitRequested()
            }
        }


        mBinding?.playerView?.findViewById<ExoSpeedView>(R.id.exo_speed)
            ?.setSpeedChangeListener(object : ExoSpeedView.SpeedChangeListener {
                override fun onSpeedChanged(selectedSpeed: Float) {
                    exoPlayerHelper?.setPlaybackSpeed(selectedSpeed)
                }
            })

        mPipModeEnterButton?.apply {
            setOnClickListener {
                videoFragmentListener.onPictureInPictureModeRequested()
            }
        }

        mBinding?.buttonSkipAd?.setOnClickListener {
            exoPlayerHelper?.getAdCurrentPosition()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.AD_SKIP_CLICK,
                    hashMapOf(
                        EventConstants.DURATION to exoPlayerHelper?.getAdCurrentPosition()
                            .toString(),
                        EventConstants.SOURCE_ID to videoData?.adResource?.adUrl.orEmpty(),
                        EventConstants.AD_ID to videoData?.adResource?.adId.orEmpty(),
                        EventConstants.PAGE to videoData?.page.orEmpty(),
                        EventConstants.VIDEO_VIEW_ID to videoData?.viewId.orEmpty(),
                        EventConstants.QUESTION_ID to videoData?.questionId.orEmpty()
                    )
                )
            )
            exoPlayerHelper?.skipAd()
        }
        mBinding?.btnAdLink?.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.AD_CTA_CLICK,
                    hashMapOf(
                        EventConstants.DURATION to exoPlayerHelper?.getAdCurrentPosition()
                            .toString(),
                        EventConstants.SOURCE_ID to videoData?.adResource?.adUrl.orEmpty(),
                        EventConstants.AD_ID to videoData?.adResource?.adId.orEmpty(),
                        EventConstants.PAGE to videoData?.page.orEmpty(),
                        EventConstants.VIDEO_VIEW_ID to videoData?.viewId.orEmpty(),
                        EventConstants.QUESTION_ID to videoData?.questionId.orEmpty()
                    )
                )
            )
            deeplinkAction.performAction(requireContext(), adDeepLink)
        }
    }

    private fun initVideoRotation() {
        changeProgressBarColor()
        mBinding?.videoContainer?.doOnLayout {
            videoViewHeight = it.height
        }
        setFullScreenStatus()
    }

    private fun setFullScreenStatus() {
        if (activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            isFullScreen = false
        } else if (activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            isFullScreen = true
        }
    }

    private fun changeProgressBarColor() {
        mBinding?.progressBarExoPlayerBuffering?.indeterminateDrawable?.setColorFilter(
            ContextCompat.getColor(requireActivity(), R.color.colorAccent),
            android.graphics.PorterDuff.Mode.MULTIPLY
        )
    }

    override fun hasToShowDateDialog() {
        if (RemoteConfigUtils.getShowExoDateDialogStatus() == "1") {
            showDateChangeDialog()
        }
    }

    private fun showDateChangeDialog() {
        if (activity == null || !isAdded) return
        Dialog(requireActivity()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val binding = DialogDateChangeBinding.inflate(layoutInflater)
            window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#cc000000")))
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            show()
            setCancelable(true)
            binding.dialogParentViewDateChange.setOnClickListener {
                if (activity == null || !isAdded) return@setOnClickListener
                dismiss()
            }
            binding.buttonDateChange.setOnClickListener {
                if (activity == null || !isAdded) return@setOnClickListener
                startActivityForResult(Intent(Settings.ACTION_DATE_SETTINGS), 0)
                dismiss()
            }
            binding.imageViewCloseDate.setOnClickListener {
                if (activity == null || !isAdded) return@setOnClickListener
                dismiss()
            }
        }
        viewModel.publishEventWith("date_change_dialog_shown", hashMapOf())
    }

    private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun updateYoutubeEngagementTime(maxSeekTime: String, engagementTime: String) {
        if (viewId.isNotEmpty()) {
            viewModel.updateVideoView(
                viewId, if (videoData != null && videoData!!.isPlayFromBackStack) "1" else "0",
                maxSeekTime,
                engagementTime, videoData?.lockUnlockLogs
            )
            if (videoData?.typeOfContent == null
                || videoData?.typeOfContent == "SF"
                || videoData?.typeOfContent == "LF"
            ) {
                viewModel.insertVideoViewStatsAndSendEvent(
                    maxSeekTime.toString(),
                    engagementTime.toString(),
                    videoData?.typeOfContent ?: "LF"
                )
            }
        } else {
            viewModel.publishVideoViewOnboarding(
                questionId!!,
                maxSeekTime,
                engagementTime,
                page,
                getStudentId()
            )
        }

    }

    override fun onYoutubePlayerEnd() {
        onPlayerEnd()
    }

    override fun onYoutubeVideoPauseInResumedState() {
        sendUserPlayerPauseEvent()
    }

    override fun onYoutubePlayerStarted() {
        isStarted = true
        closeConvivaSession()
    }

    override fun onYoutubeVideoPlayFailed(youtubeId: String, error: PlayerConstants.PlayerError) {
        if (!isStarted)
            playerFailedListener?.invoke(youtubeId, error.name)
    }

    override fun onPlayerEnd() {
        if (!isYoutube && !timeAnalytics.isEmpty()) {
            updatePlayerAnalytics(PlayerState.END)
            calculateVideoAnalytics()
        }

        if (!isYoutube) {
            // Trigger video end to Conviva
            closeConvivaSession()
        }

        mBinding?.progressBarExoPlayerBuffering?.hide()
        viewModel.publishEventWith(EventConstants.EVENT_NAME_VIDEO_PLAY_END, hashMapOf())

        videoFragmentListener.onVideoCompleted()

        sendEvent(EventConstants.EVENT_NAME_PLAY_FROM_AUTO)
        sendClevertapEvent(EventConstants.EVENT_NAME_PLAY_FROM_AUTO)
    }

    override fun registerVideoEngagementStatus(videoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {
        sendWatchData(videoEngagementStats)
    }

    override fun registerAdVideoEngagementStatus(engagementTime: Long) {
        if (engagementTime != 0L) {
            val adUuid = videoData?.adResource?.adUuid
            if (!adUuid.isNullOrBlank()) {
                viewModel.updateAdVideoView(
                    adUuid,
                    videoData?.adResource?.adId.orEmpty(),
                    engagementTime.toString()
                )
            }
        }
    }

    var actualVideoDuration: Long = 0L

    override fun onPlayerStart() {
        hidePlayerController()
        updatePlayerAnalytics(PlayerState.PLAY)
        mBinding?.progressBarExoPlayerBuffering?.hide()
        mPipModeEnterButton?.isVisible =
            videoData?.showEnterPipModeButton == true && Utils.hasPipModePermission()
        videoFragmentListener.onVideoStart()
        isStarted = true
        actualVideoDuration = exoPlayerHelper?.getDuration() ?: 0L
        setAdProgressMax(exoPlayerHelper?.getDuration()?.toInt() ?: 0)
        sendEvent(EventConstants.EVENT_NAME_VIDEO_PLAY_START)
        sendClevertapEvent(EventConstants.EVENT_NAME_VIDEO_PLAY_START)
        if (isVisualizerShowing) {
            showVisualizer()
        }
    }

    fun showVisualizer() {
        mBinding?.animation?.show()
        mBinding?.animation?.setAnimation("lottie_visualizer.zip")
        mBinding?.animation?.repeatCount = LottieDrawable.INFINITE
        mBinding?.animation?.playAnimation()
    }

    fun stopVisualizer() {
        mBinding?.animation?.pauseAnimation()
    }

    override fun onPlayerPause() {
        if (isVisualizerShowing) {
            stopVisualizer()
        }
        updatePlayerAnalytics(PlayerState.PAUSE)
        mBinding?.progressBarExoPlayerBuffering?.hide()
        videoFragmentListener.onVideoPause()
        sendEvent(EventConstants.EVENT_NAME_VIDEO_PLAY_PAUSE)
        sendClevertapEvent(EventConstants.EVENT_NAME_VIDEO_PLAY_PAUSE)
    }

    override fun onPlayerBuffering() {
        hidePlayerController()
        updatePlayerAnalytics(PlayerState.BUFFER)
        mBinding?.progressBarExoPlayerBuffering?.show()
        sendEvent(EventConstants.EVENT_NAME_VIDEO_PLAY_BUFFERING)
        sendClevertapEvent(EventConstants.EVENT_NAME_VIDEO_PLAY_BUFFERING)
    }

    override fun initConvivaAnalytics() {
        /*
        ThreadUtils.runOnAnalyticsThread {
            convivaVideoAnalytics.get().setPlayer(exoPlayerHelper?.getPlayer())
        }
        */
    }

    fun closeConvivaSession() {
        /*
        ThreadUtils.runOnAnalyticsThread {
            if (isConvivaSessionStarted) {
                convivaVideoAnalytics.get().reportPlaybackEnded()
                isConvivaSessionStarted = false
            }
        }
        */
    }

    override fun onPlayerPreparing() {
        mBinding?.progressBarExoPlayerBuffering?.show()
        /*
        if (isConvivaSessionStarted.not()) {
            ThreadUtils.runOnAnalyticsThread {
                setUpConvivaAnalytics()
            }
        }
        */
    }

    override fun onMediaSourceSelected(mediaSourceType: ExoPlayerHelper.MediaSourceType) {
        val eventName = if (mediaSourceType is ExoPlayerHelper.MediaSourceType.Hls) {
            sourceType = EventConstants.HLS_VIDEO_PLAYED
            EventConstants.HLS_VIDEO_PLAYED
        } else {
            sourceType = EventConstants.BLOB_VIDEO_PLAYED
            EventConstants.BLOB_VIDEO_PLAYED
        }
        if (eventName == EventConstants.BLOB_VIDEO_PLAYED) {
            val countToSendEvent: Int =
                Utils.getCountToSend(
                    RemoteConfigUtils.getEventInfo(),
                    EventConstants.BLOB_VIDEO_PLAYED
                )
            repeat((0 until countToSendEvent).count()) {
                sendEventByQid(eventName, questionId.toString())
            }
        } else {
            sendEventByQid(eventName, questionId.toString())
        }


        sendClevertapEventByQid(eventName, questionId.toString())
    }

    override fun onMediaSourceFailed(
        mediaSourceType: ExoPlayerHelper.MediaSourceType,
        error: ExoPlaybackException?, fromFallbackHandler: Boolean,
        hlsTimeoutTime: Long, videoUrl: String
    ) {

        /*
        error?.let {
            ThreadUtils.runOnAnalyticsThread {
                convivaVideoAnalytics.get().reportPlaybackError(
                    it.message,
                    ConvivaSdkConstants.ErrorSeverity.FATAL
                )
            }
        }
        */

        if (!isStarted)
            playerFailedListener?.invoke(videoUrl, error?.message)
        if (videoData != null && videoData!!.isPlayFromBackStack) return
        val eventName = if (mediaSourceType is ExoPlayerHelper.MediaSourceType.Hls) {
            EventConstants.HLS_FAILURE_VIDEO_PLAYED
        } else {
            EventConstants.BLOB_FAILURE_BLOB_FAILURE
        }
        viewModel.publishEventWith(eventName, hashMapOf<String, Any>().apply {
            put(Constants.PAGE, page)
            put(Constants.QUESTION_ID, questionId.toString())
            put(Constants.STUDENT_CLASS, getStudentClass())
            put(Constants.ERROR_MSG, error?.message.orEmpty())
        })
        sendEventByQid(eventName, questionId.toString(), error?.message)
        sendClevertapEventByQid(eventName, questionId.toString(), error?.message)
    }

    override fun onTimeElapsed(elapsedTimeInSeconds: Int) {
        mBinding?.fakeBufferingPercentage?.show()
        mBinding?.fakeBufferingPercentage?.text =
            "${(Math.round(((hlsTimeoutTime - elapsedTimeInSeconds) / hlsTimeoutTime.toDouble()) * 100))}%"
    }

    override fun onFallbackEnd() {
        mBinding?.fakeBufferingPercentage?.hide()
    }

    override fun onPositionDiscontinuityReasonSeek() {
        videoFragmentListener.onExoPlayerPositionDiscontinuityReasonSeek()
    }

    override fun onAdProgressUpdate(progress: Long) {
        val totalDuration = exoPlayerHelper?.getAdDuration() ?: 0
        if (skipApDuration != 0L) {
            if (progress < (skipApDuration * 1000L) && adPosition != ExoPlayerHelper.AdPosition.END) {
                mBinding?.buttonSkipAd?.isVisible = true
                mBinding?.buttonSkipAd?.text = String.format(
                    requireContext().getString(R.string.skip_in),
                    "${skipApDuration - (progress / 1000)}s"
                )
                mBinding?.buttonSkipAd?.isClickable = false
                mBinding?.buttonSkipAd?.isEnabled = false
            } else if (progress >= (skipApDuration * 1000L) && adPosition != ExoPlayerHelper.AdPosition.END) {
                mBinding?.buttonSkipAd?.isVisible = true
                mBinding?.buttonSkipAd?.text = requireContext().getString(R.string.skip)
                mBinding?.buttonSkipAd?.isClickable = true
                mBinding?.buttonSkipAd?.isEnabled = true
            } else {
                mBinding?.buttonSkipAd?.isVisible = false
                mBinding?.buttonSkipAd?.text = requireContext().getString(R.string.skip)
                mBinding?.buttonSkipAd?.isClickable = true
                mBinding?.buttonSkipAd?.isEnabled = true
            }
        }
        val formattedTime =
            getFormattedTime(TimeUnit.MILLISECONDS.toSeconds(totalDuration - progress).toInt())
        mBinding?.textViewAdTime?.text = formattedTime
    }

    override fun onAdStart() {
        /*
        ThreadUtils.runOnAnalyticsThread {
            // user wait started for conviva as Ad starts playing
            convivaVideoAnalytics.get().reportPlaybackEvent(ConvivaSdkConstants.Events.USER_WAIT_STARTED.value)
        }
        */
        videoFragmentListener.hideSuggestions()
        mBinding?.progressBarExoPlayerBuffering?.hide()
        mBinding?.textViewAdTime?.show()
        isAdStarted = true
        if (!adDeepLink.isNullOrEmpty() && activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mBinding?.adDescView?.show()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.AD_CTA_DISPLAY,
                    hashMapOf(
                        EventConstants.SOURCE_ID to videoData?.adResource?.adUrl.orEmpty(),
                        EventConstants.AD_ID to videoData?.adResource?.adId.orEmpty(),
                        EventConstants.PAGE to videoData?.page.orEmpty(),
                        EventConstants.VIDEO_VIEW_ID to videoData?.viewId.orEmpty(),
                        EventConstants.QUESTION_ID to videoData?.questionId.orEmpty()
                    )
                )
            )
        } else {
            mBinding?.adDescView?.hide()
        }
    }

    override fun onAdStop() {
        /*
        ThreadUtils.runOnAnalyticsThread {
            // user wait stopped for conviva as Ad stops playing
            convivaVideoAnalytics.get().reportPlaybackEvent(ConvivaSdkConstants.Events.USER_WAIT_ENDED.value)
        }
        */
        videoFragmentListener.showSuggestions()
    }

    override fun onAdBuffer() {
        mBinding?.progressBarExoPlayerBuffering?.show()
    }

    override fun onAdSkip() {
        mBinding?.textViewAdTime?.hide()
        mBinding?.buttonSkipAd?.hide()
    }

    private fun getFormattedTime(duration: Int): String {
        val min = (duration / 60) % 60
        val sec = (duration) % 60
        return getString(R.string.string_ad_timer, min, sec)
    }

    private fun setAdProgressMax(duration: Int?) {
        mBinding?.progressBarExoPlayerAd?.max = duration ?: 0
        val formattedVideoTime: String = duration?.let {
            getFormattedTime(duration)
        } ?: "--:--"
        mBinding?.textViewAdTime?.text = formattedVideoTime
    }

    private fun sendWatchData(userVideoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {

        val engagementTime = userVideoEngagementStats.engagementTime
        val maxSeekTime = userVideoEngagementStats.maxSeekTime
        val totalDuration = userVideoEngagementStats.duration
        val videoBytes = userVideoEngagementStats.videoBytes

        if (viewId.isNotEmpty()) {
            viewModel.updateVideoView(
                viewId, if (videoData != null && videoData!!.isPlayFromBackStack) "1" else "0",
                maxSeekTime.toString(),
                engagementTime.toString(), videoData!!.lockUnlockLogs, videoBytes.toString()
            )
            if (videoData?.typeOfContent == null
                || videoData?.typeOfContent == "SF"
                || videoData?.typeOfContent == "LF"
            ) {
                viewModel.insertVideoViewStatsAndSendEvent(
                    maxSeekTime.toString(),
                    engagementTime.toString(),
                    videoData?.typeOfContent ?: "LF"
                )
            }
        } else {
            viewModel.publishVideoViewOnboarding(
                questionId!!,
                maxSeekTime.toString(),
                engagementTime.toString(),
                page,
                getStudentId()
            )
        }

        onWatchComplete(maxSeekTime, totalDuration)
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.VIDEO_WATCH_ENGAGEMENT,
                hashMapOf(
                    EventConstants.QUESTION_ID to questionId.orEmpty(),
                    EventConstants.PAGE to page,
                    EventConstants.ENGAGEMENT_TIME to engagementTime.toString(),
                    EventConstants.MAX_SEEK_TIME to maxSeekTime.toString()
                ), ignoreMoengage = false, ignoreSnowplow = true
            )
        )
    }

    private fun onWatchComplete(videoEngagementTime: Long, videoTotalDuration: Long) {
        if (isValidWatch(videoEngagementTime, videoTotalDuration) && activity != null) {
            CountingManager.updateVideoSeenCount(requireActivity().applicationContext)
        }
    }

    private fun removeYoutubeView() {
        childFragmentManager.findFragmentById(R.id.youtubeFragmentVideoPage)?.let {
            childFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    /*
   * Return true when the user watched the video 50% or more
   * */
    private fun isValidWatch(videoEngagementTime: Long, videoTotalDuration: Long) =
        (videoEngagementTime.toFloat() / videoTotalDuration.toFloat()) >= .5

    private fun sendEvent(eventName: String) {
        getBaseTrackEvent(eventName)?.track()
    }

    private fun sendClevertapEventByQid(
        eventName: String,
        qid: String,
        errorMessage: String? = null
    ) {
        getBaseQuestionTrackEvent(eventName, qid, errorMessage)?.cleverTapTrack()
    }

    private fun sendEventByQid(eventName: String, qid: String, errorMessage: String? = null) {
        getBaseQuestionTrackEvent(eventName, qid, errorMessage)?.track()
    }

    private fun sendClevertapEvent(eventName: String, errorMessage: String? = null) {
        getBaseQuestionTrackEvent(eventName, "", errorMessage)?.track()
    }

    private fun getBaseTrackEvent(eventName: String, errorMessage: String? = null): Tracker? {
        if (activity == null || !isAdded) return null
        return (requireActivity().applicationContext as DoubtnutApp).getEventTracker()
            .addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
            .addStudentId(getStudentId())
            .addEventParameter(Constants.ERROR_MSG, errorMessage.orEmpty())
            .addScreenName(EventConstants.PAGE_VIDEO_VIEW_ACTIVITY)
    }

    private fun getBaseQuestionTrackEvent(
        eventName: String,
        qid: String,
        errorMessage: String?
    ): Tracker? {
        getBaseTrackEvent(eventName, errorMessage)?.let {
            it.addEventParameter(Constants.PAGE, page)
            it.addEventParameter(Constants.QUESTION_ID, qid)
            it.addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
            it.addEventParameter(Constants.ERROR_MSG, errorMessage.orEmpty())
            return it
        } ?: return null
    }

    private fun getStudentId(): String {
        if (activity == null || !isAdded) return ""
        return UserUtil.getStudentId()
    }

    private fun setPlayerControllerVisibilityListener() {

        gestureDetector =
            GestureDetector(mBinding?.playerView?.context, object : OnSwipeListener() {
                override fun onSwipe(direction: Direction): Boolean {
                    if (direction === Direction.up) {
                        //do your stuff
                        Log.d(TAG, "onSwipe: up")
                        if (!mBinding?.playerView?.isControllerVisible!!) {
                            mBinding?.playerView?.showController()
                        }
                        if (!isAdPlaying())
                            videoFragmentListener.showLandscapeSimilarFragment()
                        videoFragmentListener.changeLandscapeSimilarFragmentState(true)
                    }
                    if (direction === Direction.down) {
                        //do your stuff
                        Log.d(TAG, "onSwipe: down")
                        videoFragmentListener.changeLandscapeSimilarFragmentState(false)
                    }
                    return true
                }

                override fun onSingleTapUp(e: MotionEvent?): Boolean {
                    videoFragmentListener.singleTapOnPlayerView()
                    return false
                }
            })
        mBinding?.playerView?.setOnTouchListener(this)
        mBinding?.playerView?.setControllerVisibilityListener {
            if (it == View.VISIBLE) {
                videoFragmentListener.onShowPlayerControls()
            } else {
                videoFragmentListener.onHidePlayerControls()
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        //todo check default state
        val bool = mBinding?.youtubeOverlay?.onTouchEvent(event) ?: false
        if (!bool)
            gestureDetector?.onTouchEvent(event)
        return bool
    }

    fun isYoutubeVideoPlaying() = isYoutube

    fun isExoPlayerVideoPlaying(): Boolean = exoPlayerHelper?.isPlayerPlaying() ?: false

    fun isAdPlaying(): Boolean = exoPlayerHelper?.isAdPlaying() ?: false

    fun isPlayerControllerVisible() =
        mBinding?.playerView != null && mBinding?.playerView?.isControllerVisible == true

    fun showPlayerController() {
        /* no-op */
    }

    fun closeVideoQualityBottomSheet() {
        videoQualityBottomSheet?.dialog?.dismiss()
    }

    fun hidePlayerController(isInPipMode: Boolean = false) {
        if (mBinding?.playerView != null && isInPipMode) {
            mBinding?.playerView?.hideController()
        }
    }

    fun controllerAutoShow(autoShow: Boolean) {
        mBinding?.playerView?.controllerAutoShow = autoShow
    }

    private fun sendUserPlayerPauseEvent() {
        getCurrentPosition()
        val eventData = hashMapOf<String, Any>(
            Constants.VIEW_ID to viewId,
            Constants.VIDEO_TIME to getCurrentPosition(),
            Constants.VIDEO_ENGAGEMENT_TIME to getCurrentEngagementTime()
        )

        when (activity?.resources?.configuration?.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                viewModel.publishEventWith(
                    EventConstants.VIDEO_PAGE_VIDEO_PAUSE_VERTICAL,
                    eventData,
                    true
                )
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                viewModel.publishEventWith(
                    EventConstants.VIDEO_PAGE_VIDEO_PAUSE_HORIZONTAL,
                    eventData,
                    true
                )
            }
        }
    }

    private fun updatePlayerAnalytics(state: PlayerState) {

        val currentTime = System.currentTimeMillis()

        if (!timeAnalytics.isEmpty()) {
            val lastAnalytics = timeAnalytics.last
            lastAnalytics.endTime = currentTime
        }

        timeAnalytics.add(
            TimeAnalytics(
                state,
                currentTime,
                null
            )
        )
    }

    private fun calculateVideoAnalytics() {
        var totalInitializationTime = 0L
        var totalBufferingTime = 0L
        var totalPlayingTime = 0L
        var totalPauseTime = 0L

        if (actualVideoDuration > 0L) {
            actualVideoDuration.let {
                timeAnalytics.forEachIndexed { index, timeAnalytics ->
                    when (timeAnalytics.state) {
                        PlayerState.INIT -> {
                            timeAnalytics.endTime?.let {
                                totalInitializationTime += it - timeAnalytics.startTime
                            }
                        }
                        PlayerState.PLAY -> {
                            timeAnalytics.endTime?.let {
                                totalPlayingTime += it - timeAnalytics.startTime
                            } ?: (System.currentTimeMillis() - timeAnalytics.startTime)
                        }
                        PlayerState.PAUSE -> {
                            timeAnalytics.endTime?.let {
                                totalPauseTime += it - timeAnalytics.startTime
                            }
                        }
                        PlayerState.BUFFER -> {
                            timeAnalytics.endTime?.let {
                                totalBufferingTime += it - timeAnalytics.startTime
                            }
                        }
                        else -> {
                        }
                    }
                }

                val totalSpentTime =
                    totalInitializationTime + totalBufferingTime + totalPlayingTime // Not including pause time in this

                val lagRatio =
                    ((totalBufferingTime.toFloat() + totalInitializationTime.toFloat()) / (totalSpentTime).toFloat())

                viewModel.publishEventWith(
                    EventConstants.EVENT_NAME_VIDEO_ANALYTICS_V1,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.VIDEO_ANALYTICS_BUFFERING_TIME_V1, totalBufferingTime)
                        put(EventConstants.VIDEO_ANALYTICS_PLAYING_TIME_V1, totalPlayingTime)
                        put(EventConstants.VIDEO_ANALYTICS_TOTAL_SPENT_TIME_V1, totalSpentTime)
                        put(
                            EventConstants.VIDEO_ANALYTICS_TOTAL_INIT_TIME_V1,
                            totalInitializationTime
                        )
                        put(EventConstants.VIDEO_ANALYTICS_TOTAL_PAUSE_TIME_V1, totalPauseTime)
                        put(EventConstants.VIDEO_ANALYTICS_ACTUAL_DURATION_V1, it * 1000)
                        put(
                            EventConstants.VIDEO_ANALYTICS_MEDIA_SOURCE_TYPE_V1,
                            sourceType.orEmpty()
                        )
                        put(EventConstants.QUESTION_ID, questionId.toString())
                        put(EventConstants.VIDEO_VIEW_ID, viewId)
                        put(
                            EventConstants.VIDEO_ANALYTICS_LAG_RATIO_V1,
                            if (lagRatio.isNaN()) 0 else lagRatio
                        )
                        if (eventDetail?.eventMap != null) {
                            putAll(eventDetail?.eventMap!!)
                        }
                    },
                    true
                )
            }
        } else {
            timeAnalytics.forEachIndexed { index, timeAnalytics ->
                when (timeAnalytics.state) {
                    PlayerState.INIT -> {
                        timeAnalytics.endTime?.let {
                            totalInitializationTime += it - timeAnalytics.startTime
                        }
                    }
                    else -> {
                    }
                }
            }

            viewModel.publishEventWith(
                EventConstants.VIDEO_SCREEN_EXIT_BEFORE_PLAYING,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.VIDEO_ANALYTICS_TOTAL_INIT_TIME_V1, totalInitializationTime)
                    put(EventConstants.VIDEO_VIEW_ID, viewId)
                    if (eventDetail?.eventMap != null) {
                        putAll(eventDetail?.eventMap!!)
                    }
                },
                true
            )
        }

        timeAnalytics.clear()
    }

    private fun isYesterday(d: Date): Boolean {
        return DateUtils.isToday(d.time + DateUtils.DAY_IN_MILLIS)
    }

    private fun addSeekPositionObserver() {
        disposable.add(
            Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (::videoFragmentListener.isInitialized) {
                        val position = exoPlayerHelper?.getPlayerCurrentPosition()
                        if (position != null && position != lastPosition) {
                            videoFragmentListener.onSeekPositionChange(position)
                        }
                        lastPosition = position
                    }
                }
        )
    }

    override fun onProgress(positionMs: Long) {
        videoFragmentListener.onExoPlayerProgress(positionMs)
    }

    private fun addTimeBarScrubListener() {
        mBinding?.playerView?.findViewById<DefaultTimeBar>(R.id.exo_progress)
            ?.addListener(object : TimeBar.OnScrubListener {
                override fun onScrubStart(timeBar: TimeBar, position: Long) {
                    videoFragmentListener.onExoPlayerTimeBarSrubStart(timeBar, position)
                }

                override fun onScrubMove(timeBar: TimeBar, position: Long) { /* no-op */
                }

                override fun onScrubStop(
                    timeBar: TimeBar,
                    position: Long,
                    canceled: Boolean
                ) { /* no-op */
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVideoBinding {
        return FragmentVideoBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): VideoFragmentViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding?.youtubeOverlay?.performListener(object : YouTubeOverlay.PerformListener {
            override fun onAnimationStart() {
                mBinding?.playerView?.useController = false
                mBinding?.youtubeOverlay?.visibility = View.VISIBLE
            }

            override fun onAnimationEnd() {
                mBinding?.youtubeOverlay?.visibility = View.GONE
                mBinding?.playerView?.useController = true
            }
        })

        mBinding?.playerView?.doubleTapDelay = 800
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                closeConvivaSession()
                false
            } else false
        }
    }

}
