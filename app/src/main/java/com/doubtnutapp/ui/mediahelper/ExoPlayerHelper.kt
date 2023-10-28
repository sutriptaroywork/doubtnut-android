package com.doubtnutapp.ui.mediahelper

import android.content.Context
import android.net.Uri
import android.os.CountDownTimer
import android.os.Handler
import android.os.Parcelable
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.EXO_PLAYER_FAILED
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.*
import com.doubtnutapp.data.camerascreen.datasource.LocalConfigDataSource
import com.doubtnutapp.doubletapplayerview.youtube.YouTubeOverlay
import com.doubtnutapp.downloadedVideos.ExoDownloadTracker
import com.doubtnutapp.exoplayer.extensions.okhttp.OkHttpDataSourceFactory
import com.doubtnutapp.networkstats.models.VideoStatsData
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.video.TrackSelectionDialog
import com.google.ads.interactivemedia.v3.api.Ad
import com.google.ads.interactivemedia.v3.api.AdErrorEvent
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.util.Util
import dagger.Lazy
import kotlinx.android.parcel.Parcelize
import okhttp3.*
import java.io.IOException
import java.lang.Long.max
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ExoPlayerHelper(
    val context: Context,
    val playerView: PlayerView,
    private val defaultMinBufferMs: Int = DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
    private val defaultMaxBufferMs: Int = DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
    private val reBufferDuration: Int? = null
) : Player.EventListener, PlaybackPreparer, LifecycleObserver {

    private val ONE_SECONDS: Long = 1000
    private val EXOPLAYER_FALLBACK_TIMEOUT = 12L.toSeconds() /*seconds*/
    private val EXOPLAYER_SLOWEST_SPEED = 0.25f
    private val EXOPLAYER_FASTEST_SPEED = 2.0f

    private var exoPlayer: SimpleExoPlayer? = null
    private var adsPlayer: SimpleExoPlayer? = null
    private var engageTime: Long = 0
    private var adEngageTime: Long = 0
    private var adRunningTimeInMillis: Long = 0
    private var maxSeekTime: Long = 0
    private var runningTimeInMillis: Long = 0
    private var videoEngagementStats: VideoEngagementStats? = null
    private var videoPlayBackSpeed = 1.0f
    private var trackSelector: DefaultTrackSelector? = null
    private var mediaSourceType: MediaSourceType = MediaSourceType.Hls
    private var drmScheme: String? = null
    private var drmLicenseUrl: String? = null

    private var exoPlayerStateListener: ExoPlayerStateListener? = null
    private var exoPlayerProgressListener: ProgressListener? = null
    private var progressDelayInMs: Long = 1000L
    private var controllerVisibilityListener: PlayerControlView.VisibilityListener? = null
    private var adProgressUpdateLister: AdProgressUpdateLister? = null
    private var mediaSourceStatusListener: MediaSourceStatusListener? = null
    private var videoEngagementStatusListener: VideoEngagementStatusListener? = null
    private var adVideoEngagementStatusListener: AdVideoEngagementStatusListener? = null
    private var fallbackElapsedTimeListener: FallbackElapsedTimeListener? = null
    private var positionDiscontinuityListener: PositionDiscontinuityListener? = null

    private var currentPos: Long = 0

    private var currentWindow: Int = 0
    private var isNewVideo = false

    private val adProgressHandler = Handler()
    private var hlsFallbackHandler: CountDownTimer? = null

    private val progressHandler = Handler()
    private var progressRunnable: Runnable? = null

    private var videoUri: Uri = Uri.parse("")
    private var questionId: String = ""
    private var videoName: String = ""
    private var contentType: String = ""
    private var fallbackUri: Uri = Uri.parse("")
    private var adsUri: Uri? = null

    private var overlay: YouTubeOverlay? = null

    private var hlsTimeoutTime = EXOPLAYER_FALLBACK_TIMEOUT

    private var useFallback = true

    private var isShowingTrackSelectionDialog = false

    //to keep track whether we notified about the MediaSource successful run for a single unique video
    //because player to Player.STATE_READ many time during a complete video we need to notify it once
    private var isMediaSourceSuccessNotified = false

    private var videoStartTime: Long? = null
    private var overrideMaxSeekTime: Boolean = false

    var canResumePlayerOnLifecycleResume = true
    private var adPosition: AdPosition = AdPosition.START
    private var adContentStartDuration = 10_000L

    private var inPictureInPictureMode = false

    private var canEnterInPIP = true

    private var loopCurrentVideo: Boolean = false

    private var videoBytes: Long = 0

    // Region - Start IMA Extension
    // IMA ad tag uri
    private var imaAdTagUri: Uri = Uri.parse("")

    // IMA ad tag timeout to avoid blocking of playing content
    private var imaAdMediaLoadTimeoutInMs: Int = DEFAULT_IMA_AD_MEDIA_LOAD_TIME_OUT_IN_MS

    // total duration of currently playing IMA ad
    private var currentImaAdDuration: Long = 0

    // total time to which current IMA has played
    private var totalTimeCurrentImaAdPlayed: Long = 0

    // total playing time of all the IMA ads at any point
    private var totalTimeImaAdsPlayed: Long = 0

    // sum of duration of all the IMA ads till now
    private var totalDurationOfAllImaAds: Long = 0

    // check whether an IMA ad is currently playing or not
    private var isImaAdPlaying = false

    @Inject
    lateinit var imaAdsLoaderBuilder: Lazy<ImaAdsLoader.Builder>

    private val imaAdErrorEventListener: AdErrorEvent.AdErrorListener =
        AdErrorEvent.AdErrorListener { p0 ->
            android.util.Log.e("Ad Error : ", p0?.error?.message.orEmpty())
            imaAdEventListener?.onError(getImaErrorData(p0?.error?.message))
        }

    private val imaAdVideoPlayerCallback: VideoAdPlayer.VideoAdPlayerCallback =
        object : VideoAdPlayer.VideoAdPlayerCallback {

            override fun onAdProgress(p0: AdMediaInfo?, p1: VideoProgressUpdate?) {
                when (p1?.currentTimeMs) {
                    -1L -> {}
                    else -> {
                        currentImaAdDuration = p1?.currentTimeMs ?: 0
                    }
                }
            }

            override fun onPlay(p0: AdMediaInfo?) {}

            override fun onVolumeChanged(p0: AdMediaInfo?, p1: Int) {}

            override fun onPause(p0: AdMediaInfo?) {}

            override fun onLoaded(p0: AdMediaInfo?) {}

            override fun onResume(p0: AdMediaInfo?) {}

            override fun onEnded(p0: AdMediaInfo?) {
                calculateImaAdEngagementTime()
            }

            override fun onContentComplete() {
                calculateImaAdEngagementTime()
            }

            override fun onError(p0: AdMediaInfo?) {
                calculateImaAdEngagementTime()
            }

            override fun onBuffering(p0: AdMediaInfo?) {}
        }

    private val adEventListener: AdEvent.AdEventListener =
        AdEvent.AdEventListener {
            val imaAdData = getImaAdData(it?.ad)
            when (it.type) {
                AdEvent.AdEventType.LOADED -> {
                    // AdEventType.LOADED will be fired when ads are ready to be
                    // played. AdsManager.start() begins ad playback. This method is
                    // ignored for VMAP or ad rules playlists, as the SDK will
                    // automatically start executing the playlist.
                    imaAdEventListener?.onLoaded(imaAdData)

                    // calculate total duration of IMA ads appears till now
                    totalDurationOfAllImaAds += (imaAdData?.duration?.toLong() ?: 0L) * 1000
                }
                AdEvent.AdEventType.STARTED -> {
                    isImaAdPlaying = true
                    imaAdEventListener?.onStarted(imaAdData)
                }
                AdEvent.AdEventType.PAUSED -> {
                    imaAdEventListener?.onPaused(imaAdData)
                }
                AdEvent.AdEventType.RESUMED -> {
                    isImaAdPlaying = true
                    imaAdEventListener?.onResumed(imaAdData)
                }
                AdEvent.AdEventType.COMPLETED -> {
                    imaAdEventListener?.onCompleted(imaAdData)
                }
                AdEvent.AdEventType.SKIPPED -> {
                    isImaAdPlaying = false
                    imaAdEventListener?.onSkipped(imaAdData)
                }
                AdEvent.AdEventType.CONTENT_PAUSE_REQUESTED -> {
                    // AdEventType.CONTENT_PAUSE_REQUESTED is fired immediately before a video ad is played.
                    imaAdEventListener?.onContentPauseRequested(imaAdData)
                }
                AdEvent.AdEventType.CONTENT_RESUME_REQUESTED -> {
                    // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is
                    // completed and you should start playing your content.
                    imaAdEventListener?.onContentResumeRequested(imaAdData)
                }
                AdEvent.AdEventType.ALL_ADS_COMPLETED -> {
                    isImaAdPlaying = false
                    imaAdEventListener?.onAllAdsCompleted(imaAdData)
                }
                AdEvent.AdEventType.TAPPED -> {
                    imaAdEventListener?.onAdTapped(imaAdData)
                }
                AdEvent.AdEventType.ICON_TAPPED -> {
                    imaAdEventListener?.onIconTapped(imaAdData)
                }
                else -> {}
            }
        }

    private val imaAdsLoader: ImaAdsLoader by lazy {
        imaAdsLoaderBuilder.get()
            .setAdEventListener(adEventListener)
            .setVideoAdPlayerCallback(imaAdVideoPlayerCallback)
            .setAdErrorListener(imaAdErrorEventListener)
            .setVastLoadTimeoutMs(imaAdMediaLoadTimeoutInMs)
            .setMediaLoadTimeoutMs(imaAdMediaLoadTimeoutInMs) // if IMA ad fails to load in this time (millisecond), content video will start playing
            .build()
    }

    private var imaAdEventListener: ImaAdEventListener? = null

    fun setImaAdEventListener(imaAdEventListener: ImaAdEventListener) {
        this.imaAdEventListener = imaAdEventListener
    }

    private fun calculateImaAdEngagementTime() {
        totalTimeCurrentImaAdPlayed += currentImaAdDuration
        totalTimeImaAdsPlayed += totalTimeCurrentImaAdPlayed

        resetCurrentImaEngagement()
    }

    private fun resetCurrentImaEngagement() {
        currentImaAdDuration = 0
        totalTimeCurrentImaAdPlayed = 0
    }

    private fun resetAllImaEngagement() {
        currentImaAdDuration = 0
        totalTimeCurrentImaAdPlayed = 0
        totalTimeImaAdsPlayed = 0
        totalDurationOfAllImaAds = 0
    }

    private fun getImaAdData(ad: Ad?): ImaAdData? {
        if (ad == null) return null
        return ImaAdData.Builder()
            .adId(ad.adId)
            .adPosition(ad.adPodInfo?.adPosition)
            .totalAd(ad.adPodInfo?.totalAds)
            .creativeId(ad.creativeId)
            .creativeAdId(ad.creativeAdId)
            .contentType(ad.contentType)
            .width(ad.width)
            .height(ad.height)
            .title(ad.title)
            .description(ad.description)
            .duration(ad.duration)
            .isSkippable(ad.isSkippable)
            .skipTimeOffset(ad.skipTimeOffset)
            .build()
    }

    private fun getImaErrorData(message: String?): ImaAdData? {
        if (message.isNullOrEmpty()) return null
        return ImaAdData.Builder()
            .errorMessage(message)
            .build()
    }

    // Set up the factory for media sources, passing the ads loader and ad view providers.
    // val dataSourceFactory : DataSource.Factory = DefaultDataSource.Factory(context)
    private val dataSourceFactory: DataSource.Factory by lazy { DefaultDataSourceFactory(context) }

    private val mediaSourceFactory: MediaSourceFactory by lazy {
        DefaultMediaSourceFactory(dataSourceFactory)
            .setAdsLoaderProvider { imaAdsLoader }
            .setAdViewProvider(playerView)
    }

    /**
     * set IMA ad tag url, IMA extension wraps this url itself with video content
     * and start playing ads with video content.
     */
    fun setImaAdTagUrl(adTagUrl: String) {
        require(adTagUrl.isNotEmpty())
        imaAdTagUri = getVastTagAdUrl(adTagUrl)
    }

    /**
     * set IMA ad media load timeout, if ad media fails to load in this time
     * video content will start playing
     */
    fun setImaAdMediaLoadTimeout(timeout: Int) {
        require(timeout > 0)
        imaAdMediaLoadTimeoutInMs = timeout
    }

    private fun releaseImaAdsLoader() {
        imaAdsLoader.setPlayer(null)
        imaAdsLoader.release()
        resetAllImaEngagement()
    }

    private fun getVastTagAdUrl(adTagUrl: String): Uri {
        val prefix = "<![CDATA["
        val postfix = "]]"
        return Uri.parse("$prefix$adTagUrl$postfix")
    }
    // Region - End IMA Extension

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    companion object {
        /**
         * Do not use HTTP/2 protocol with ExoPlayer, which is included in the default setup of OkHttp
         * Using it and rapidly seeking for a while on video or having multiple videos autoplay in a RecyclerView
         * results in SocketTimeoutException
         * @see "https://github.com/google/ExoPlayer/issues/4078"
         */
        private val okhttpClient = OkHttpClient.Builder()
            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
            .build()

        /*
        this is called to make a connection by the okhttpclient used in exoplayer to the cdn for video urls,
        which results in faster requests when connection is reused later on
         */
        fun warmupClient() {
            val cdnUrl = defaultPrefs().getString(LocalConfigDataSource.VIDEO_CDN_BASE_URL, null)
                ?: return
            okhttpClient.newCall(Request.Builder().url(cdnUrl).head().build())
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {}

                    override fun onResponse(call: Call, response: Response) {}
                })
        }

        private fun getOkHttpDataSourceFactory(transferListener: TransferListener? = null): DataSource.Factory =
            OkHttpDataSourceFactory(
                okhttpClient,
                Util.getUserAgent(DoubtnutApp.INSTANCE, "doubtnutapp"), transferListener
            )

        fun getUpStreamDataSourceFactory(mediaSourceType: MediaSourceType): DataSource.Factory {
            return when (mediaSourceType) {
                MediaSourceType.Hls -> getOkHttpDataSourceFactory(DefaultBandwidthMeter())
                MediaSourceType.Dash -> getOkHttpDataSourceFactory(DefaultBandwidthMeter())
                MediaSourceType.Blob -> getOkHttpDataSourceFactory()
                MediaSourceType.Rtmp -> RtmpDataSourceFactory()
                MediaSourceType.DashOffline -> getOkHttpDataSourceFactory(DefaultBandwidthMeter())
            }
        }

        fun getMediaSourceType(mediaType: String) = when (mediaType) {
            MEDIA_TYPE_HLS -> {
                MediaSourceType.Hls
            }
            MEDIA_TYPE_DASH -> {
                MediaSourceType.Dash
            }
            MEDIA_TYPE_RTMP -> {
                MediaSourceType.Rtmp
            }
            MEDIA_TYPE_DASH_OFFLINE -> {
                MediaSourceType.DashOffline
            }
            else -> {
                MediaSourceType.Blob
            }
        }

        val SPEED_LIST = arrayListOf(0.75f, 1f, 1.25f, 1.5f, 2f, 3f)

        // IMA sdk
        const val DEFAULT_IMA_AD_MEDIA_LOAD_TIME_OUT_IN_MS = 5000
    }

    fun showTrackSelectionDialog(fragmentManager: FragmentManager) {
        if (!isShowingTrackSelectionDialog
            && TrackSelectionDialog.willHaveContent(trackSelector)
        ) {
            isShowingTrackSelectionDialog = true
            val trackSelectionDialog:
                    TrackSelectionDialog =
                TrackSelectionDialog.createForTrackSelector(trackSelector) {
                    isShowingTrackSelectionDialog = false
                }
            trackSelectionDialog.show(fragmentManager,  /* tag= */null)
        }
    }

    fun setOverlayView(overlay: YouTubeOverlay) {
        this.overlay = overlay
    }

    private val adProgressRunnable = object : Runnable {
        override fun run() {
            adsPlayer?.also {
                adProgressUpdateLister?.onAdProgressUpdate((it.currentPosition))
                adProgressHandler.postDelayed(this, 1000)
            } ?: pauseAdPositionTracker()
        }
    }

    fun setAudioAttributes(audioAttributes: AudioAttributes, audioFocus: Boolean) {
        if (exoPlayer == null) {
            initExoPLayer()
            overlay?.player(exoPlayer!!)
        }
        exoPlayer?.setAudioAttributes(audioAttributes, audioFocus)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                stopProgressUpdate()
                sendEngagementTime()
            }
            Player.STATE_BUFFERING -> {
                exoPlayerStateListener?.onPlayerBuffering()
                if (playWhenReady) {
                    pauseTrackingEngagementTime()
                }
            }
            Player.STATE_ENDED -> {
                stopProgressUpdate()
                sendEngagementTime()
                if (adPosition == AdPosition.END && adsUri != null) {
                    playAd()
                } else
                    exoPlayerStateListener?.onPlayerEnd()
            }
            Player.STATE_READY -> {
                when (playWhenReady) {
                    true -> {
                        startProgressUpdate()
                        if (!isMediaSourceSuccessNotified) {
                            isMediaSourceSuccessNotified = true
                            mediaSourceStatusListener?.onMediaSourceSelected(mediaSourceType)
                        }
                        startTrackingEngagementTime()
                        exoPlayerStateListener?.onPlayerStart()
                        stopFallbackHandler()
                    }
                    false -> {
                        stopProgressUpdate()
                        pauseTrackingEngagementTime()
                        exoPlayerStateListener?.onPlayerPause()
                    }
                }
            }
        }
    }

    fun setPipMode(isEnter: Boolean) {
        inPictureInPictureMode = isEnter
    }

    fun canEnterInPIP() = canEnterInPIP

    override fun preparePlayback() {
        exoPlayer?.retry()
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        mediaSourceStatusListener?.onMediaSourceFailed(
            mediaSourceType,
            error,
            false,
            -1,
            videoUri.toString()
        )

        //if Blob is also failed then there is no sense to handle the fallback as it was our last resort
        if (mediaSourceType !is MediaSourceType.Blob) {
            handleFallback()
        } else {
            exoPlayerStateListener?.onPlayerError(error)
        }

        try {
            if (!NetworkUtils.isConnected(context) || error == null) {
                return
            }
            Log.e(error)
            DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(
                AnalyticsEvent(
                    EXO_PLAYER_FAILED,
                    hashMapOf(EventConstants.ERROR to error.toString()), ignoreSnowplow = true
                )
            )
            if ((error.cause as HttpDataSource.HttpDataSourceException).cause.toString() == "javax.net.ssl.SSLHandshakeException: Chain validation failed") {
                mediaSourceStatusListener?.hasToShowDateDialog()
            }
        } catch (e: Exception) {

        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onLifecycleOwnerStart() {
        if (exoPlayer == null) {
            ExoPlayerCacheManager.getInstance(context).stopCaching()
            if (isDataPresent()) {
                initPlayer()
                initAdPlayer()
                preparePlayer()
            }
        }
    }

    private fun isDataPresent() = videoUri.isValid() && fallbackUri.isValid()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLifecycleOwnerResume() {
        resumePlay()
    }

    fun resumePlay() {
        if (canResumePlayerOnLifecycleResume) {
            adsPlayer?.playWhenReady = true
            if (adsPlayer?.isPlaying != true) {
                resumePlayer()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onLifecycleOwnerStop() {
        adsPlayer?.playWhenReady = false
        pausePlayer()
        sendEngagementTime()
        sendAdVideoEngagement()
        currentPos = exoPlayer?.currentPosition ?: 0
        currentWindow = exoPlayer?.currentWindowIndex ?: 0
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onLifecycleOwnerDestroy() {
        releasePlayer(false)
    }

    private fun startAdPositionTracker() {
        adRunningTimeInMillis = System.currentTimeMillis()
        if (adProgressUpdateLister == null) return
        adProgressHandler.postDelayed(adProgressRunnable, ONE_SECONDS)
    }

    private fun pauseAdPositionTracker() {
        if (adRunningTimeInMillis != 0L) {
            adEngageTime += (System.currentTimeMillis() - adRunningTimeInMillis)
            adRunningTimeInMillis = 0
        }

        if (adProgressUpdateLister == null) return
        adProgressHandler.removeCallbacks(adProgressRunnable)
    }

    override fun onSeekProcessed() {
        if (overrideMaxSeekTime
            && videoStartTime != null
            && videoStartTime!! < System.currentTimeMillis()
        ) {
            maxSeekTime = System.currentTimeMillis() - videoStartTime!!
        } else {
            val currentPosition = getPlayerCurrentPosition()
            if (currentPosition >= maxSeekTime) {
                maxSeekTime = currentPosition
            }
        }
    }

    fun setMediaData(
        mediaType: String, drmScheme: String? = null, drmLicenseUrl: String? = null,
        useFallback: Boolean = true, videoStartTime: Long? = null,
        overrideMaxSeekTime: Boolean = false
    ) {
        mediaSourceType = getMediaSourceType(mediaType)
        this.drmScheme = drmScheme
        this.drmLicenseUrl = drmLicenseUrl
        this.useFallback = useFallback
        this.videoStartTime = videoStartTime
        this.overrideMaxSeekTime = overrideMaxSeekTime
    }

    fun setVideoUrl(videoUrl: String) {
        require(videoUrl.isNotEmpty())
        videoUri = Uri.parse(videoUrl)
    }

    fun sendVideoDetails(id: String, name: String, content: String) {
        questionId = id
        videoName = name
        contentType = content
    }

    fun enableLoopCurrentVideo() {
        loopCurrentVideo = true
    }

    fun setAdUrl(adUrl: String) {
        adsUri = adUrl.toUri()
    }

    fun setAdPosition(adPosition: AdPosition) {
        this.adPosition = adPosition
    }

    fun setAdContentStartPosition(durationInSec: Long) {
        adContentStartDuration = TimeUnit.SECONDS.toMillis(durationInSec)
    }

    fun setFallbackUrl(fallbackVideoUrl: String) {
        fallbackUri = Uri.parse(fallbackVideoUrl)
    }

    @Deprecated("Video starts playing in lifecycle ON_START itself")
    fun startPlayingVideo() {
//        mediaSourceType = MediaSourceType.Hls
//        preparePlayer()
    }

    fun onTrackSelection(
        mediaSource: String,
        videoUrl: String,
        drmScheme: String?,
        drmLicenseUrl: String?,
        useFallback: Boolean
    ) {
        setMediaData(
            mediaType = mediaSource, drmScheme = drmScheme,
            drmLicenseUrl = drmLicenseUrl, useFallback = useFallback
        )
        setVideoUrl(videoUrl)
        preparePlayer()
    }

    fun videoChanged() {
        isMediaSourceSuccessNotified = false
        isNewVideo = true
        currentWindow = 0
        currentPos = 0
        sendEngagementTime()
        releasePlayer(false)
    }

    fun setPlaybackSpeed(speed: Float) {
        if (speed !in SPEED_LIST) return
        this.videoPlayBackSpeed = speed
        exoPlayer?.playbackParameters = PlaybackParameters(videoPlayBackSpeed, 1.0f)
    }

    fun getPlayerCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }

    fun setPlayerCurrentPosition(position: Long) {
        this.currentPos = position
    }

    fun getPlayerCurrentWindow(): Int {
        return exoPlayer?.currentWindowIndex ?: -1
    }

    fun pausePlayer() {
        exoPlayer?.playWhenReady = false
    }

    fun resumePlayer() {
        exoPlayer?.playWhenReady = true
    }

    fun isPlayerPlaying(): Boolean = exoPlayer?.isPlaying ?: false

    fun isAdPlaying(): Boolean = adsPlayer?.isPlaying ?: false

    fun stop() {
        exoPlayer?.stop()
    }

    fun mute() {
        exoPlayer?.volume = 0f
    }

    fun unMute() {
        exoPlayer?.volume = 1f
    }

    fun isMute(): Boolean = exoPlayer?.volume == 0f

    fun next() {
        val timeline = exoPlayer?.currentTimeline
        if (timeline?.isEmpty == true) {
            return
        }
        val nextWindowIndex = exoPlayer?.nextWindowIndex
        if (nextWindowIndex != C.INDEX_UNSET) {
            exoPlayer?.seekTo(nextWindowIndex!!, C.TIME_UNSET)
        }
    }

    fun setExoPlayerStateListener(stateListener: ExoPlayerHelper.ExoPlayerStateListener) {
        exoPlayerStateListener = stateListener
    }

    /**
     * Use this method to get ExoPlayer progressListener updates. No internal ExoPlayer progress update
     * listener is provided as the player updates its internal position at a very high rate
     * because this is required for media playback.
     * @see "https://github.com/google/ExoPlayer/issues/2090"
     * @see ProgressListener
     *
     * @param progressDelayMs The delay between subsequent progress updates
     * @param progressListener ProgressListener used for progress updates
     */
    fun setProgressListener(progressDelayMs: Long, progressListener: ProgressListener) {
        exoPlayerProgressListener = progressListener
        progressDelayInMs = progressDelayMs
        progressRunnable = Runnable {
            exoPlayerProgressListener?.let {
                val currentPos = getPlayerCurrentPosition()
                it.onProgress(currentPos)
                if (adPosition == AdPosition.MID && adsUri != null && currentPos >= adContentStartDuration) {
                    playAd()
                }
                if (exoPlayer?.playWhenReady == true) {
                    progressRunnable?.let { it1 ->
                        progressHandler.postDelayed(
                            it1,
                            progressDelayInMs
                        )
                    }
                }
            }
        }
    }

    /**
     * Use this method to start providing progress updates after setting progress listener
     */
    private fun startProgressUpdate() {
        if (progressRunnable == null || exoPlayerProgressListener == null) return
        progressHandler.postDelayed(progressRunnable!!, progressDelayInMs)
    }

    /**
     * Use this method to stop providing progress updates
     */
    private fun stopProgressUpdate() {
        if (progressRunnable == null || exoPlayerProgressListener == null) return
        progressHandler.removeCallbacks(progressRunnable!!)
    }

    fun setControllerVisibilityListener(listener: PlayerControlView.VisibilityListener) {
        controllerVisibilityListener = listener
        playerView.setControllerVisibilityListener {
            controllerVisibilityListener?.onVisibilityChange(it)
        }
    }

    fun setAdProgressUpdateListener(adProgressUpdateLister: AdProgressUpdateLister) {
        this@ExoPlayerHelper.adProgressUpdateLister = adProgressUpdateLister
    }

    fun setMediaSourceStatusListener(mediaSourceStatusListener: MediaSourceStatusListener) {
        this@ExoPlayerHelper.mediaSourceStatusListener = mediaSourceStatusListener
    }

    fun setVideoEngagementStatusListener(videoEngagementStatusListener: VideoEngagementStatusListener) {
        this@ExoPlayerHelper.videoEngagementStatusListener = videoEngagementStatusListener
    }

    fun setAdVideoEngagementStatusListener(adVideoEngagementStatusListener: AdVideoEngagementStatusListener) {
        this@ExoPlayerHelper.adVideoEngagementStatusListener = adVideoEngagementStatusListener
    }

    fun setFallbackElapsedTimeListener(fallbackElapsedTimeListener: FallbackElapsedTimeListener) {
        this@ExoPlayerHelper.fallbackElapsedTimeListener = fallbackElapsedTimeListener
    }

    fun setPositionDiscontinuityListener(positionDiscontinuityListener: PositionDiscontinuityListener) {
        this.positionDiscontinuityListener = positionDiscontinuityListener
    }

    fun setHlsTimeoutTime(timeOut: Long) {
        hlsTimeoutTime = timeOut.toSeconds()
        // fallback to blob if hls timeout is 0
        if (hlsTimeoutTime == 0L && mediaSourceType == MediaSourceType.Hls && useFallback) {
            mediaSourceType = MediaSourceType.Blob
        }
    }

    fun unSetListeners() {
        exoPlayerStateListener = null
        exoPlayerProgressListener = null
        controllerVisibilityListener = null
        videoEngagementStatusListener = null
        mediaSourceStatusListener = null
        adProgressUpdateLister = null
    }

    fun getPlayer() = exoPlayer

    fun getDuration(): Long {
        val duration = exoPlayer?.duration ?: 0
        return TimeUnit.MILLISECONDS.toSeconds(if (duration == C.TIME_UNSET) 0 else duration)
    }

    fun getAdDuration(): Long =
        adsPlayer?.duration ?: 0

    fun getAdCurrentPosition(): Long =
        adsPlayer?.currentPosition ?: 0

    //Returns engagement time in millis, without pausing the engagement time tracking
    fun getCurrentEngagementTime() =
        if (runningTimeInMillis == 0L) {
            engageTime
        } else {
            engageTime + (System.currentTimeMillis() - runningTimeInMillis)
        }

    private fun initPlayer() {
        if (exoPlayer == null) {
            initExoPLayer()
            overlay?.player(exoPlayer!!)
            exoPlayerStateListener?.initConvivaAnalytics()
        }
    }

    fun initExoPLayer() {
        trackSelector = DefaultTrackSelector(AdaptiveTrackSelection.Factory())
        trackSelector?.parameters = DefaultTrackSelector.ParametersBuilder().build()

        var loadControl = DefaultLoadControl()

        if (FeaturesManager.isFeatureEnabled(context, Features.PLAYER_BUFFER_TIME)) {
            val playerBufferPayload =
                FeaturesManager.getFeaturePayload(context, Features.PLAYER_BUFFER_TIME)
            if (playerBufferPayload != null) {
                val initialBufferDuration =
                    playerBufferPayload["initial_buffer_duration"] as? Double
                val rebufferDuration = playerBufferPayload["rebuffer_duration"] as? Double
                if (initialBufferDuration != null && rebufferDuration != null) {
                    // defaultMaxBufferMs should be >= defaultMinBufferMs
                    // defaultMinBufferMs should be >= initialBufferDuration
                    loadControl = DefaultLoadControl.Builder().setBufferDurationsMs(
                        defaultMinBufferMs,
                        defaultMaxBufferMs,
                        if (defaultMinBufferMs >= initialBufferDuration.toInt()) initialBufferDuration.toInt() else defaultMinBufferMs,
                        reBufferDuration ?: rebufferDuration.toInt()
                    ).build()
                }
            }
        }

        exoPlayer = SimpleExoPlayer.Builder(context, DefaultRenderersFactory(context))
            .setLoadControl(loadControl)
            .setTrackSelector(trackSelector!!)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        if (loopCurrentVideo) {
            exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    fun preparePlayer() {
        //double ensuring ExoPlayer is initialised
        initPlayer()

        //if its still not initialised we the error message to the user
        //and return
        if (exoPlayer == null) {
            ToastUtils.makeText(
                context,
                R.string.string_exoplayerHelper_errorPlayingVideo,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        exoPlayerStateListener?.onPlayerPreparing()
        exoPlayer!!.playWhenReady = true
        exoPlayer!!.setMediaSource(buildMediaSource(), false)
        playerView.player = exoPlayer

        // IMA extension
        imaAdsLoader.setPlayer(exoPlayer)

        exoPlayer!!.seekTo(currentWindow, currentPos)
        exoPlayer!!.addListener(this)
        exoPlayer!!.addAnalyticsListener(object : AnalyticsListener {
            override fun onLoadStarted(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)
                videoBytes += loadEventInfo.bytesLoaded
                android.util.Log.e("Exoplayer1", videoBytes.toString())
            }

            override fun onLoadCanceled(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                super.onLoadCanceled(eventTime, loadEventInfo, mediaLoadData)
                videoBytes += loadEventInfo.bytesLoaded
                android.util.Log.e("Exoplayer2", videoBytes.toString())
            }

            override fun onLoadError(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData,
                error: IOException,
                wasCanceled: Boolean
            ) {
                super.onLoadError(eventTime, loadEventInfo, mediaLoadData, error, wasCanceled)
                if (wasCanceled) {
                    videoBytes += loadEventInfo.bytesLoaded
                    android.util.Log.e("Exoplayer3", videoBytes.toString())
                }
            }

            override fun onLoadCompleted(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                super.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData)
                videoBytes += loadEventInfo.bytesLoaded
                android.util.Log.e("Exoplayer4", videoBytes.toString())
            }
        })
        playerView.setPlaybackPreparer(this)
        isNewVideo = false
        videoEngagementStats = VideoEngagementStats.init()
        if (adsUri != null && adPosition == AdPosition.START) {
            playAd()
        } else {
            exoPlayer!!.prepare()
        }
    }

    private fun releasePlayer(isFromFallback: Boolean) {
        if (exoPlayer != null) {
            exoPlayer?.removeListener(this)
            exoPlayer?.release()
            exoPlayer = null
            if (!isFromFallback) {
                engageTime = 0
                maxSeekTime = 0
                videoBytes = 0
            }
            runningTimeInMillis = 0
            stopFallbackHandler()
            playerView.player = null
        }
        pauseAdPositionTracker()
        releaseAdPlayer()
        releaseImaAdsLoader()
    }

    private fun releaseAdPlayer() {
        if (adsPlayer != null) {
            adsPlayer?.removeListener(adsListener)
            adsPlayer?.release()
            adsPlayer = null
        }
    }

    private fun playAd() {
        if (inPictureInPictureMode || adsUri == null)
            return
        if (adsPlayer == null)
            initAdPlayer()
        playerView.player = adsPlayer
        adsPlayer?.playWhenReady = true
        pausePlayer()
        adsPlayer?.prepare()
        playerView.useController = false
        playerView.controllerAutoShow = false
        playerView.controllerHideOnTouch = false
        playerView.hideController()
        canEnterInPIP = false
    }

    private fun playVideo() {
        adsUri = null
        pauseAdPositionTracker()
        adsPlayer?.playWhenReady = false
        playerView.player = exoPlayer
        resumePlayer()
        releaseAdPlayer()
        playerView.controllerAutoShow = true
        playerView.controllerHideOnTouch = true
        playerView.useController = true
        canEnterInPIP = true
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    private fun initAdPlayer() {
        if (adsUri == null)
            return
        if (adsPlayer != null) {
            releaseAdPlayer()
        }
        adsPlayer = SimpleExoPlayer.Builder(context)
            .build()
        adsPlayer?.addListener(adsListener)
        exoPlayer!!.addAnalyticsListener(object : AnalyticsListener {
            override fun onLoadStarted(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)
                videoBytes += loadEventInfo.bytesLoaded
                android.util.Log.e("Exoplayer1i", videoBytes.toString())
            }

            override fun onLoadCanceled(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                super.onLoadCanceled(eventTime, loadEventInfo, mediaLoadData)
                videoBytes += loadEventInfo.bytesLoaded
                android.util.Log.e("Exoplayer2i", videoBytes.toString())
            }

            override fun onLoadError(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData,
                error: IOException,
                wasCanceled: Boolean
            ) {
                super.onLoadError(eventTime, loadEventInfo, mediaLoadData, error, wasCanceled)
                if (wasCanceled) {
                    videoBytes += loadEventInfo.bytesLoaded
                    android.util.Log.e("Exoplayer3i", videoBytes.toString())
                }

            }

            override fun onLoadCompleted(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                super.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData)
                videoBytes += loadEventInfo.bytesLoaded
                android.util.Log.e("Exoplayer4i", videoBytes.toString())
            }
        })
        val mediaItem = MediaItem.fromUri(adsUri!!)
        val mediaSource = ProgressiveMediaSource.Factory(getOkHttpDataSourceFactory())
            .createMediaSource(mediaItem)
        adsPlayer?.setMediaSource(mediaSource)
        //adsPlayer?.prepare()
        playerView.setPlaybackPreparer(this)
    }

    private val adsListener = object : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            when (playbackState) {
                Player.STATE_ENDED -> {
                    skipAd()
                    sendAdVideoEngagement()
                    adProgressUpdateLister?.onAdStop()
                }
                Player.STATE_IDLE -> {
                    sendAdVideoEngagement()
                }
                Player.STATE_READY -> {
                    when (playWhenReady) {
                        true -> {
                            adProgressUpdateLister?.onAdStart()
                            startAdPositionTracker()
                        }
                        false -> {
                            adProgressUpdateLister?.onAdStop()
                            pauseAdPositionTracker()
                        }
                    }
                }
                Player.STATE_BUFFERING -> {
                    adProgressUpdateLister?.onAdBuffer()
                    pauseAdPositionTracker()
                }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            super.onPlayerError(error)
            Log.e(error.cause!!, "exoplayer")
            skipAd()
        }

    }

    fun skipAd() {
        playVideo()
        if (adPosition == AdPosition.END)
            exoPlayerStateListener?.onPlayerEnd()
        else (adPosition == AdPosition.START)
        exoPlayer?.prepare()
        adProgressUpdateLister?.onAdSkip()
    }

    private fun buildMediaSource(): MediaSource {
        val upstreamFactory = getUpStreamDataSourceFactory(mediaSourceType)
        val downloadReq = ExoDownloadTracker.getInstance(context)
            .getDownloadRequest(videoUri)

        // ToDo - will use this for testing
//        val imaAdTagUri = Uri.parse(context.resources.getString(R.string.ad_tag_vmap_pre_roll_single_ad_mid_roll_optimized_pod_with_3_ads_post_roll_single_ad_bumpers_around_all_ad_breaks))
        // ignoring cache in dash videos for now
        val mediaItem = downloadReq?.toMediaItem() ?: MediaItem.fromUri(videoUri)

        return when (mediaSourceType) {
            is MediaSourceType.DashOffline -> {
                android.util.Log.e("MediaType", "DO")
                val callback = LocalMediaDrmCallback(downloadReq!!.keySetId!!)
                val drmSessionManager = DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(
                        Util.getDrmUuid(drmScheme!!)!!,
                        FrameworkMediaDrm.DEFAULT_PROVIDER
                    )
                    .build(callback)
                drmSessionManager.setMode(
                    DefaultDrmSessionManager.MODE_PLAYBACK,
                    downloadReq.keySetId
                )

                AdsMediaSource(
                    DashMediaSource.Factory(wrappedCacheDataSourceFactory(upstreamFactory))
                        .setDrmSessionManager(drmSessionManager)
                        .createMediaSource(mediaItem),
                    DataSpec(imaAdTagUri),
                    "",
                    mediaSourceFactory,
                    imaAdsLoader,
                    playerView
                )
            }
            is MediaSourceType.Hls -> {
                if (isNewVideo) { // we are going to start the fallback timer if its the old video
                    startFallbackHandler()
                }
                AdsMediaSource(
                    HlsMediaSource.Factory(wrappedCacheDataSourceFactory(upstreamFactory))
                        .setDrmSessionManager(getDrmSessionManager(drmScheme, drmLicenseUrl))
                        .createMediaSource(videoUri),
                    DataSpec(imaAdTagUri),
                    "",
                    mediaSourceFactory,
                    imaAdsLoader,
                    playerView
                )

            }
            is MediaSourceType.Dash -> {
                // ignoring cache in dash videos for now
                AdsMediaSource(
                    DashMediaSource.Factory(upstreamFactory)
                        .setDrmSessionManager(getDrmSessionManager(drmScheme, drmLicenseUrl))
                        .createMediaSource(videoUri),
                    DataSpec(imaAdTagUri),
                    "",
                    mediaSourceFactory,
                    imaAdsLoader,
                    playerView
                )
            }
            is MediaSourceType.Rtmp -> {
                AdsMediaSource(
                    ProgressiveMediaSource.Factory(upstreamFactory)
                        .setDrmSessionManager(getDrmSessionManager(drmScheme, drmLicenseUrl))
                        .createMediaSource(videoUri),
                    DataSpec(imaAdTagUri),
                    "",
                    mediaSourceFactory,
                    imaAdsLoader,
                    playerView
                )
            }
            else -> {
                AdsMediaSource(
                    ProgressiveMediaSource.Factory(wrappedCacheDataSourceFactory(upstreamFactory))
                        .setDrmSessionManager(getDrmSessionManager(drmScheme, drmLicenseUrl))
                        .createMediaSource(fallbackUri),
                    DataSpec(imaAdTagUri),
                    "",
                    mediaSourceFactory,
                    imaAdsLoader,
                    playerView
                )
            }
        }

    }

    private fun wrappedCacheDataSourceFactory(upstreamFactory: DataSource.Factory): DataSource.Factory {
        return CacheDataSource.Factory().setCache(ExoPlayerCacheManager.getInstance(context).cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    private fun getDrmSessionManager(
        drmScheme: String?,
        drmLicenseUrl: String?
    ): DrmSessionManager {
        return if (drmLicenseUrl.isNullOrBlank() || drmScheme.isNullOrBlank()) {
            DrmSessionManager.getDummyDrmSessionManager()
        } else {
            val mediaDrmCallback: MediaDrmCallback = createMediaDrmCallback(drmLicenseUrl, null)
            DefaultDrmSessionManager.Builder()
                .setUuidAndExoMediaDrmProvider(
                    Util.getDrmUuid(drmScheme)!!,
                    FrameworkMediaDrm.DEFAULT_PROVIDER
                )
                .setMultiSession(false)
                .build(mediaDrmCallback)
        }
    }

    private fun createMediaDrmCallback(
        licenseUrl: String, keyRequestPropertiesArray: Array<String>?
    ): HttpMediaDrmCallback {
        val licenseDataSourceFactory: HttpDataSource.Factory = OkHttpDataSourceFactory(
            okhttpClient,
            Util.getUserAgent(DoubtnutApp.INSTANCE, "doubtnutapp")
        )
        val drmCallback = HttpMediaDrmCallback(licenseUrl, licenseDataSourceFactory)
        if (keyRequestPropertiesArray != null) {
            var i = 0
            while (i < keyRequestPropertiesArray.size - 1) {
                drmCallback.setKeyRequestProperty(
                    keyRequestPropertiesArray[i],
                    keyRequestPropertiesArray[i + 1]
                )
                i += 2
            }
        }
        return drmCallback
    }

    private fun startFallbackHandler() {
        if (useFallback) {
            hlsFallbackHandler = object : CountDownTimer(hlsTimeoutTime, ONE_SECONDS) {
                override fun onFinish() {
                    handleFallback()
                    mediaSourceStatusListener?.onMediaSourceFailed(
                        MediaSourceType.Hls,
                        null, true, hlsTimeoutTime, videoUri.toString()
                    )
                }

                override fun onTick(millisUntilFinished: Long) {
                    fallbackElapsedTimeListener?.onTimeElapsed((millisUntilFinished / 1000).toInt())
                }
            }.also {
                it.start()
            }
        }
    }

    private fun handleFallback() {
        if (useFallback) {
            releasePlayer(true)
            prepareBlobMediaSourcePlayer()
        }
    }

    private fun prepareBlobMediaSourcePlayer() {
        mediaSourceType = MediaSourceType.Blob
        preparePlayer()
    }

    private fun startTrackingEngagementTime() {
        runningTimeInMillis = System.currentTimeMillis()
    }

    private fun pauseTrackingEngagementTime() {

        if (runningTimeInMillis == 0L) return //tracking has not started yet

        engageTime += (System.currentTimeMillis() - runningTimeInMillis)
        runningTimeInMillis = 0
    }

    private fun stopFallbackHandler() {
        hlsFallbackHandler?.cancel()
        fallbackElapsedTimeListener?.onFallbackEnd()
    }

    private fun getEngagementTime(): Long {
        pauseTrackingEngagementTime()
        return TimeUnit.MILLISECONDS.toSeconds(getEngagementTimeInMs())
    }

    private fun getEngagementTimeInMs(): Long {
        if (totalTimeImaAdsPlayed == 0L) { // case where user leaves the screen without watching first ad
            totalTimeImaAdsPlayed += currentImaAdDuration
        } else if (totalTimeCurrentImaAdPlayed == 0L) { // case where user leaves the screen in between mid or post roll ad
            totalTimeImaAdsPlayed += currentImaAdDuration
        }

        val videoEngagementTime =
            if (engageTime - totalTimeImaAdsPlayed < 1000) { // where very less difference, consider as 0
                0L
            } else if (totalDurationOfAllImaAds - totalTimeImaAdsPlayed < 1000) { // where very less difference, consider max of both
                engageTime - max(totalTimeImaAdsPlayed, totalDurationOfAllImaAds)
            } else { // consider as it is
                engageTime - totalTimeImaAdsPlayed
            }

        return videoEngagementTime
    }

    private fun getMaxSeekTime(): Long {
        onSeekProcessed()
        // if ima ad is playing right now and user leaves the screen, exoplayer return seek time of currently playing ad,
        // that is why, subtract current ad time from max seek time till now.
        // if ad is not playing right now, no need to subtract.
        val finalMaxSeekTime = if (isImaAdPlaying) {
            (maxSeekTime - currentImaAdDuration).coerceAtLeast(getEngagementTimeInMs())
        } else {
            maxSeekTime
        }
        return TimeUnit.MILLISECONDS.toSeconds(finalMaxSeekTime)
    }

    override fun onPositionDiscontinuity(reason: Int) {

        //variable to check is it required to create the new VideoEngagementStats placeHolder
        val needVideoEngagementStats =
            reason == Player.DISCONTINUITY_REASON_SEEK && videoEngagementStats?.isConsumedNot == false

        if (needVideoEngagementStats) {
            videoEngagementStats = videoEngagementStats?.resetIsConsumedNot()
        }

        if (reason == Player.DISCONTINUITY_REASON_SEEK || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
            positionDiscontinuityListener?.onPositionDiscontinuityReasonSeek()
        }
    }

    fun resetEngagementTime() {
        engageTime = 0
    }

    private fun sendEngagementTime() {
        videoEngagementStats = videoEngagementStats?.update(
            engagementTime = getEngagementTime(),
            maxSeekTime = getMaxSeekTime(),
            duration = getDuration(),
            videoBytes = videoBytes,
        )

        resetAllImaEngagement()

        videoEngagementStats?.let {
            videoEngagementStatusListener?.registerVideoEngagementStatus(it)
        }

        DoubtnutApp.INSTANCE.runOnDifferentThread {
            if (videoBytes != 0L) {
                if (questionId.isNotEmpty() || videoName.isNotEmpty()) {
                    DoubtnutApp.INSTANCE.getDatabase()?.videoNetworkDao()?.saveData(
                        VideoStatsData(
                            questionId = questionId,
                            videoName = videoName,
                            videoBytes = videoBytes,
                            videoUrl = videoUri.toString(),
                            date = System.currentTimeMillis(),
                            engagementTime = engageTime,
                            seekTime = maxSeekTime,
                            contentType = contentType
                        )
                    )
                    DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(
                        AnalyticsEvent(
                            CoreEventConstants.NETWORK_DATA_SENT,
                            hashMapOf(
                                CoreEventConstants.VIDEO_BYTES to videoBytes,
                                CoreEventConstants.QUESTION_ID to questionId,
                                CoreEventConstants.VIDEO_NAME to videoName,
                                CoreEventConstants.VIDEO_URL to videoUri
                            ),
                            ignoreSnowplow = true
                        )
                    )
                }
            }
            videoBytes = 0
        }
    }

    private fun sendAdVideoEngagement() {
        if (adEngageTime != 0L) {
            adVideoEngagementStatusListener?.registerAdVideoEngagementStatus(
                TimeUnit.MILLISECONDS.toSeconds(
                    adEngageTime
                )
            )
        }
    }

    interface ExoPlayerStateListener {
        fun onPlayerPreparing() {}
        fun onPlayerStart()
        fun onPlayerPause()
        fun onPlayerEnd()
        fun onPlayerBuffering()
        fun initConvivaAnalytics() {}
        fun onPlayerError(error: ExoPlaybackException) {}
    }

    interface ProgressListener {
        fun onProgress(positionMs: Long)
    }

    interface AdProgressUpdateLister {
        fun onAdProgressUpdate(progress: Long)
        fun onAdStart()
        fun onAdStop()
        fun onAdSkip()
        fun onAdBuffer()
    }

    interface MediaSourceStatusListener {
        fun onMediaSourceSelected(mediaSourceType: MediaSourceType)
        fun onMediaSourceFailed(
            mediaSourceType: MediaSourceType, error: ExoPlaybackException?,
            fromFallbackHandler: Boolean, hlsTimeoutTime: Long, videoUrl: String
        )

        fun hasToShowDateDialog()
    }

    interface VideoEngagementStatusListener {
        fun registerVideoEngagementStatus(videoEngagementStats: VideoEngagementStats)
    }

    interface AdVideoEngagementStatusListener {
        fun registerAdVideoEngagementStatus(engagementTime: Long)
    }

    interface FallbackElapsedTimeListener {
        fun onTimeElapsed(elapsedTimeInSeconds: Int)
        fun onFallbackEnd()
    }

    interface PositionDiscontinuityListener {
        fun onPositionDiscontinuityReasonSeek()
    }

    sealed class MediaSourceType {
        object Hls : MediaSourceType()
        object Blob : MediaSourceType()
        object Dash : MediaSourceType()
        object Rtmp : MediaSourceType()
        object DashOffline : MediaSourceType()
    }

    @Parcelize
    enum class AdPosition : Parcelable {
        START,
        MID,
        END
    }

    data class VideoEngagementStats(
        val engagementTime: Long = 0,
        val maxSeekTime: Long = 0,
        val duration: Long = 0,
        val isConsumedNot: Boolean = true,
        val videoBytes: Long = 0,
    ) {

        companion object {
            fun init() = VideoEngagementStats()
            var sum1: Long = 0L
            var sum2: Long = 0L
            var sum3: Long = 0L
            var sum4: Long = 0L

            var sum1i: Long = 0L
            var sum2i: Long = 0L
            var sum3i: Long = 0L
            var sum4i: Long = 0L
        }

        fun update(
            engagementTime: Long,
            maxSeekTime: Long,
            duration: Long,
            videoBytes: Long,
        ) =
            copy(
                engagementTime = engagementTime,
                maxSeekTime = maxSeekTime,
                duration = duration,
                isConsumedNot = false,
                videoBytes = videoBytes,
            )

        fun resetIsConsumedNot() = init()
    }
}
