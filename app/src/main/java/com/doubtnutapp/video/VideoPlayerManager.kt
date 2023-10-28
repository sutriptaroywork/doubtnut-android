package com.doubtnutapp.video

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.domain.videoPage.entities.AnalysisData
import com.doubtnutapp.domain.videoPage.entities.EventDetails
import com.doubtnutapp.ui.mediahelper.*
import com.doubtnutapp.videoPage.model.AdResource
import com.doubtnutapp.videoPage.model.ImaAdTagResourceData
import com.doubtnutapp.videoPage.model.VideoResource
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 01/11/20.
 */
class VideoPlayerManager(
    val fm: FragmentManager,
    private val videoFragmentListener: VideoFragmentListener,
    @IdRes val containerViewId: Int,
    val playerTypeOrMediaTypeChangedListener: PlayerTypeOrMediaTypeChangedListener,
    val openWebViewOnVideoFail: OpenWebViewOnVideoFail? = null
) {

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    private val isWebViewDisabled = FeaturesManager.isFeatureEnabled(
        DoubtnutApp.INSTANCE,
        Features.DISABLE_WEBVIEW_ON_VIDEO_ERROR
    )

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var videoFragment: VideoFragment? = null
        private set

    val currentPlayerPosition
        get() = videoFragment?.getCurrentPosition() ?: 0

    val videoDuration
        get() = videoFragment?.getVideoDuration() ?: 0

    val isPlayerControllerVisible
        get() = videoFragment?.isPlayerControllerVisible() ?: false

    val isYoutubeVideoPlaying
        get() = videoFragment?.isYoutubeVideoPlaying() ?: false

    val isExoPlayerVideoPlaying: Boolean
        get() = videoFragment?.isExoPlayerVideoPlaying() ?: false

    val currentEngagementTime
        get() = videoFragment?.getCurrentEngagementTime() ?: 0

    private var id = ""
    private var videoResourceList: List<VideoResource>? = null
    private var lockUnlockLogs: String? = null
    private var analysisData: AnalysisData? = null
    private var viewId = ""
    private var startPositionInSeconds: Long = 0
    private var isPlayedFromTheBackStack = false
    private var page = ""
    private var aspectRatio: String = VideoFragment.DEFAULT_ASPECT_RATIO
    private var eventDetail: EventDetails? = null
    private var showFullScreen = true
    private var mControllerAutoShow: Boolean = true
    private var mShowEnterPipModeButton: Boolean = false
    private var isInPipMode: Boolean = false
    private var blockForwarding: Boolean = false
    private var ncertExperiment: Boolean = false
    private var fromViewHolder: Boolean? = false

    private var answerId: String? = null
    private var expertId: String? = null
    private var videoName: String? = null
    private var isPremium: Boolean? = null
    private var isVip: Boolean? = null
    private var chapter: String? = null
    private var subject: String? = null
    private var bottomView: String? = null
    private var videoLanguage: String? = null
    private var isLive: Boolean? = false
    private var typeOfContent: String? = null
    private var ocrText: String? = null

    // Track errors wrt urls played [url, error]
    private var errorMap = hashMapOf<String, String>()

    var isRtmpPlaying: Boolean = false
        private set

    var isTimeShiftVideoPlaying = false
        private set

    private var startTime: Long? = null

    var ongoingVideoResource: VideoResource? = null
        private set

    private val playFailedListener: PlayFailedListener = { resourceId, error ->
        // Add resource in error map as key, if error occurs, add it as value corresponding to resource key.
        error?.let { errorMap[resourceId] = it }
        sendPlayFailedEvent()
        playVideoFromResource()
    }

    private val switchToTimeShiftListener: SwitchToTimeShiftListener = { moveToTimeShiftUrl(it) }

    private val goLiveListener: GoLiveListener = { goLive() }

    private var adResource: AdResource? = null

    private var imaAdTagResource: List<ImaAdTagResourceData>? = null

    fun setAndInitPlayFromResource(
        id: String,
        videoResourceList: List<VideoResource>,
        viewId: String,
        startPositionInSeconds: Long,
        isPlayedFromTheBackStack: Boolean,
        page: String,
        aspectRatio: String?,
        eventDetail: EventDetails?,
        startTime: Long?,
        showFullScreen: Boolean,
        controllerAutoShow: Boolean = true,
        showEnterPipModeButton: Boolean = false,
        adResource: AdResource? = null,
        isInPipMode: Boolean = false,
        blockForwarding: Boolean = false,
        ncertExperiment: Boolean = false,
        answerId: String? = null,
        expertId: String? = null,
        videoName: String? = null,
        isPremium: Boolean? = false,
        isVip: Boolean? = false,
        subject: String? = null,
        chapter: String? = null,
        bottomView: String? = null,
        videoLanguage: String? = null,
        viewHolder: Boolean? = false,
        isLive: Boolean? = false,
        typeOfContent: String? = null,
        ocrText: String? = null,
        lockUnlockLogs: String? = null,
        analysisData: AnalysisData? = null,
        imaAdTagResource: List<ImaAdTagResourceData>? = null
    ) {
        // Empty errorMap
        errorMap.clear()

        this.id = id
        this.videoResourceList = videoResourceList
        this.lockUnlockLogs = lockUnlockLogs
        this.analysisData = analysisData
        this.viewId = viewId
        this.startPositionInSeconds = startPositionInSeconds
        this.isPlayedFromTheBackStack = isPlayedFromTheBackStack
        this.page = page
        this.fromViewHolder = viewHolder
        this.startTime = startTime
        this.ocrText = ocrText
        if (aspectRatio.isNullOrBlank()) {
            this.aspectRatio = VideoFragment.DEFAULT_ASPECT_RATIO
        } else {
            this.aspectRatio = aspectRatio
        }
        this.adResource = adResource
        this.eventDetail = eventDetail
        this.showFullScreen = showFullScreen
        mControllerAutoShow = controllerAutoShow
        mShowEnterPipModeButton = showEnterPipModeButton
        this.isInPipMode = isInPipMode
        this.blockForwarding = blockForwarding
        this.ncertExperiment = ncertExperiment
        this.answerId = answerId
        this.expertId = expertId
        this.videoName = videoName
        this.isPremium = isPremium
        this.isVip = isVip
        this.subject = subject
        this.chapter = chapter
        this.bottomView = bottomView
        this.videoLanguage = videoLanguage
        this.isLive = isLive
        this.typeOfContent = typeOfContent
        this.imaAdTagResource = imaAdTagResource
        playVideoFromResource()
    }

    private fun playVideoFromResource() {
        videoResourceList
            ?.none { !it.isPlayed }
            ?.apply {
                if (this) {
                    // Send Event if all the urls has been tried to play once
                    sendPlayListFailedEvent()
                    // Open WebView to play video url
                    if (!isWebViewDisabled) {
                        openWebViewOnVideoFail?.invoke()
                    }
                    videoResourceList?.map { it.isPlayed = false }
                }
            }
        val nextVideo = videoResourceList
            ?.firstOrNull {
                !it.isPlayed
            } ?: videoResourceList?.firstOrNull()
        if (nextVideo != null) {
            ongoingVideoResource = nextVideo
            nextVideo.isPlayed = true
            isRtmpPlaying = nextVideo.mediaType == MEDIA_TYPE_RTMP
            isTimeShiftVideoPlaying = false
            if (nextVideo.mediaType == MEDIA_TYPE_YOUTUBE) {
                playVideoWithYoutube(nextVideo.resource, nextVideo.offset)
            } else {
                playVideoWithExo(
                    nextVideo.resource,
                    nextVideo.mediaType,
                    nextVideo.drmScheme,
                    nextVideo.drmLicenseUrl,
                    nextVideo.dropDownList,
                    nextVideo.offset
                )
            }
        }
    }

    private fun playVideoWithYoutube(youtubeID: String, offsetValue: Long?) {
        playerTypeOrMediaTypeChangedListener(
            PLAYER_TYPE_YOUTUBE,
            MEDIA_TYPE_YOUTUBE
        )
        val initialOffsetInSec = getInitialOffset(startPositionInSeconds, offsetValue)
        videoFragment = VideoFragment.newYoutubeInstance(
            VideoFragment.Companion.YoutubeVideoData(
                youtubeId = youtubeID,
                showFullScreen = showFullScreen,
                viewId = viewId,
                startSeconds = initialOffsetInSec.toFloat()
            ), videoFragmentListener, playFailedListener
        )
        fm.beginTransaction().replace(containerViewId, videoFragment!!).commitAllowingStateLoss()
    }

    fun removeVideoFromContainer() {
        videoFragment?.let {
            fm.beginTransaction().remove(it).commitAllowingStateLoss()
        }
    }

    private fun playVideoWithExo(
        videoUrl: String,
        mediaType: String?,
        drmScheme: String?,
        drmLicenseUrl: String?,
        playBackUrlList: List<VideoResource.PlayBackData>? = null,
        offsetValue: Long?
    ) {
        playerTypeOrMediaTypeChangedListener(
            PLAYER_TYPE_EXO,
            mediaType.orDefaultValue(MEDIA_TYPE_BLOB)
        )
        val initialOffsetInSec = getInitialOffset(startPositionInSeconds, offsetValue)
        val videoData = VideoFragment.Companion.VideoData(
            questionId = id,
            videoUrl = videoUrl,
            fallbackVideoUrl = videoUrl,
            aspectRatio = aspectRatio,
            show_fullscreen = showFullScreen,
            startPosition = TimeUnit.SECONDS.toMillis(initialOffsetInSec),
            page = page,
            isPlayFromBackStack = isPlayedFromTheBackStack,
            viewId = viewId,
            mediaType = mediaType,
            drmScheme = drmScheme,
            drmLicenseUrl = drmLicenseUrl,
            useFallBack = false,
            addDummyProgress = isRtmpPlaying,
            showGoLiveText = isTimeShiftVideoPlaying,
            playBackUrlList = playBackUrlList,
            videoStartTime = playStartTime(),
            overrideMaxSeekTime = isRtmpPlaying,
            hideProgressDuration = isTimeShiftVideoPlaying || isRtmpPlaying,
            controllerAutoShow = mControllerAutoShow,
            showEnterPipModeButton = mShowEnterPipModeButton,
            adResource = adResource,
            isInPipMode = isInPipMode,
            blockForwarding = blockForwarding,
            ncertExperiment = ncertExperiment,
            answerId = answerId,
            expertId = expertId,
            videoName = videoName,
            isPremium = isPremium,
            isVip = isVip,
            chapter = chapter,
            subject = subject,
            videoLanguage = videoLanguage,
            bottomView = bottomView,
            fromViewHolder = fromViewHolder,
            isLive = isLive,
            typeOfContent = typeOfContent,
            ocrText = ocrText,
            lockUnlockLogs = lockUnlockLogs,
            analysisData = analysisData,
            imaAdTagResourceData = imaAdTagResource
        )
        videoFragment = VideoFragment.newInstance(
            videoData, eventDetail,
            videoFragmentListener, playFailedListener, switchToTimeShiftListener, goLiveListener
        )
        fm.beginTransaction().replace(containerViewId, videoFragment!!).commitAllowingStateLoss()
    }

    private fun playStartTime(): Long? {
        return if (isRtmpPlaying) {
            startTime
        } else {
            null
        }
    }

    private fun moveToTimeShiftUrl(position: Long) {
        val currentResource = ongoingVideoResource ?: return
        val currentTime = System.currentTimeMillis()
        if (startTime == null || startTime!! > currentTime) return
        val currentTimeInSec = currentTime / 1000
        val startTimeInSec = startTime!! / 1000
        val getTimeShiftPosition = ((100 - position) * (currentTimeInSec - startTimeInSec)) / 100
        val timeShiftUrl = currentResource.timeShiftResource?.resource
        if (!timeShiftUrl.isNullOrBlank()) {
            val timeShiftUrlAtPosition = "$timeShiftUrl?delay=$getTimeShiftPosition"
            isRtmpPlaying = false
            isTimeShiftVideoPlaying = true
            playVideoWithExo(
                timeShiftUrlAtPosition,
                currentResource.timeShiftResource.mediaType,
                currentResource.timeShiftResource.drmScheme,
                currentResource.timeShiftResource.drmLicenseUrl,
                null,
                null
            )
        }
    }

    private fun goLive() {
        if (ongoingVideoResource != null && ongoingVideoResource?.mediaType != MEDIA_TYPE_YOUTUBE) {
            val currentResource = ongoingVideoResource ?: return
            isRtmpPlaying = currentResource.mediaType == MEDIA_TYPE_RTMP
            isTimeShiftVideoPlaying = false
            playVideoWithExo(
                currentResource.resource,
                currentResource.mediaType,
                currentResource.drmScheme,
                currentResource.drmLicenseUrl,
                currentResource.dropDownList,
                null
            )
        }
    }

    fun loadCacheVideo(videoData: VideoFragment.Companion.VideoData) {
        videoFragment = VideoFragment.newInstance(videoData, null, videoFragmentListener)
        fm.beginTransaction().replace(containerViewId, videoFragment!!).commitAllowingStateLoss()
    }

    fun updateData(videoData: VideoFragment.Companion.VideoData) =
        videoFragment?.updateData(videoData)

    fun enterFullScreen() = videoFragment?.enterFullscreen()

    fun exitFullScreen() = videoFragment?.exitFullscreen()

    fun resetVideo() = videoFragment?.resetVideo()

    fun closeConvivaSession() = videoFragment?.closeConvivaSession()

    fun showPlayerController() = videoFragment?.showPlayerController()

    fun hidePlayerController(isInPipMode: Boolean = false) =
        videoFragment?.hidePlayerController(isInPipMode)

    fun controllerAutoShow(autoShow: Boolean) = videoFragment?.controllerAutoShow(autoShow)

    fun onWindowFocusChanged(hasFocus: Boolean) = videoFragment?.onWindowFocusChanged(hasFocus)

    fun pauseExoPlayer() = videoFragment?.exoPlayerHelper?.pausePlayer()

    fun resumeExoPlayer() = videoFragment?.exoPlayerHelper?.resumePlayer()

    fun getExoPlayerPlaybackState(): Int? =
        videoFragment?.exoPlayerHelper?.getPlayer()?.playbackState

    fun setPipStatus(boolean: Boolean) = videoFragment?.exoPlayerHelper?.setPipMode(boolean)

    fun canEnterInPIP() = videoFragment?.exoPlayerHelper?.canEnterInPIP() ?: true

    fun mute(isMute: Boolean) =
        if (isMute) videoFragment?.exoPlayerHelper?.mute() else videoFragment?.exoPlayerHelper?.unMute()

    fun startExoPlayerVideoFromBeginning() {
        videoFragment?.exoPlayerHelper?.getPlayer()?.seekTo(0, 0)
        resumeExoPlayer()
    }

    private fun getInitialOffset(startPositionInSeconds: Long, offsetValue: Long?): Long {
        return if (startPositionInSeconds != 0L) {
            startPositionInSeconds
        } else {
            offsetValue ?: 0L
        }
    }

    fun setExoCurrentPosition(position: Long) {
        videoFragment?.setExoCurrentPosition(position)
    }

    private fun sendPlayFailedEvent() {
        val currentResourceData = ongoingVideoResource ?: return
        val event = AnalyticsEvent(
            EventConstants.VIDEO_PLAY_LIST_FAILED,
            hashMapOf(
                EventConstants.EVENT_NAME_ID to id,
                EventConstants.VIDEO_RESOURCE to currentResourceData.resource,
                EventConstants.VIDEO_MEDIA_TYPE to currentResourceData.mediaType.orEmpty()
            ), ignoreSnowplow = true
        )
        analyticsPublisher.publishEvent(event)
    }

    private fun sendPlayListFailedEvent() {
        val event = AnalyticsEvent(
            EventConstants.NO_VIDEO_PLAYED_IN_LIST,
            hashMapOf(
                EventConstants.EVENT_NAME_ID to id,
                Constants.CAUSE to Gson().toJson(errorMap).toString(),
                Constants.IS_API_ERROR to false,
                EventConstants.PAGE to page,
                EventConstants.VIDEO_VIEW_ID to viewId
            ), ignoreSnowplow = true
        )
        analyticsPublisher.publishEvent(event)
    }

}