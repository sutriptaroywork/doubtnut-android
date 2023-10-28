package com.doubtnutapp.video

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.databinding.FragmentSimpleVideoBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_HLS
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.audio.AudioAttributes
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.parcel.Parcelize
import javax.inject.Inject

class SimpleVideoFragment : BaseBindingFragment<DummyViewModel, FragmentSimpleVideoBinding>(),
    ExoPlayerHelper.ExoPlayerStateListener {

    companion object {

        private const val VIDEO_DATA = "video_data"

        const val DEFAULT_ASPECT_RATIO = "16:9"
        const val TAG = "SimpleVideoFragment"

        fun newInstance(videoData: VideoData, videoFragmentListener: VideoFragmentListener, source: String?, isFromFeed: Boolean) = SimpleVideoFragment().apply {
            this.videoFragmentListener = videoFragmentListener
            arguments = Bundle().apply {
                putParcelable(VIDEO_DATA, videoData)
                putString(Constants.SOURCE, source)
                putBoolean("isFromFeed", isFromFeed)
            }
        }

        @Parcelize
        data class VideoData(val videoUrl: String,
                             val aspectRatio: String = DEFAULT_ASPECT_RATIO,
                             val autoPlay: Boolean = true,
                             val startPosition: Long = 0) : Parcelable

    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var exoPlayerHelper: ExoPlayerHelper? = null

    private var videoData: VideoData? = null

    private lateinit var videoFragmentListener: VideoFragmentListener

    private var initCompleted: Boolean = false

    private var startTime: Long = -1

    private var appStateObserver: Disposable? = null
    protected var isAppInForeground: Boolean = true


    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        appStateObserver = DoubtnutApp.INSTANCE
                .bus()?.toObservable()?.subscribe { `object` ->
                    if (`object` is ApplicationStateEvent) {
                        isAppInForeground = (`object` as ApplicationStateEvent).state
                        if (isAppInForeground) {
                            startTime = System.currentTimeMillis()
                        } else {
                            sendEnagementTracking()
                        }
                    }
                }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        statusbarColor(requireActivity(), R.color.black)
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


        if (requireArguments().getParcelable<VideoData>(VIDEO_DATA) != null) {

            // exoplayer setup
            initVideo()
            mBinding?.videoContainer?.show()
            this.videoData = requireArguments().getParcelable<VideoData>(VIDEO_DATA)!!
            setupExoPlayer()

        } else {
            return
        }
    }

    private fun initVideo() {
        exoPlayerHelper = ExoPlayerHelper(requireActivity(), mBinding?.playerView!!)
        lifecycle.addObserver(exoPlayerHelper!!)
        initCompleted = true
    }

    @SuppressLint("CheckResult")
    private fun setupExoPlayer() {
        exoPlayerHelper!!.setMediaData(MEDIA_TYPE_HLS)
        exoPlayerHelper!!.setVideoUrl(videoData!!.videoUrl)
        exoPlayerHelper!!.setFallbackUrl(videoData!!.videoUrl)
        exoPlayerHelper!!.setPlayerCurrentPosition(videoData!!.startPosition)
        if (videoData!!.autoPlay) {
            exoPlayerHelper!!.startPlayingVideo()
        }
        mBinding?.playerView?.findViewById<ImageView>(R.id.exo_fullscreen)?.visibility = View.GONE
    }

    fun startPlaying() {
        if (!initCompleted) return
        exoPlayerHelper?.startPlayingVideo()
    }

    fun resetVideo() {
        if (!initCompleted) return
        exoPlayerHelper?.videoChanged()
    }

    override fun onResume() {
        super.onResume()
        if (!initCompleted) return
        exoPlayerHelper?.setAudioAttributes(getAudioAttributes(), true)
    }

    override fun onPause() {
        super.onPause()
        mBinding?.progressBarExoPlayerBuffering?.hide()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        if (!initCompleted) return
        exoPlayerHelper?.unSetListeners()
        super.onStop()
    }

    private fun initializePlayer() {
        exoPlayerHelper!!.setExoPlayerStateListener(this)
        mBinding?.playerView?.findViewById<ImageView>(R.id.exo_fullscreen)?.hide()
    }

    override fun onPlayerEnd() {
        mBinding?.progressBarExoPlayerBuffering?.hide()
        videoFragmentListener.onVideoCompleted()
    }

    override fun onPlayerStart() {
        mBinding?.progressBarExoPlayerBuffering?.hide()
        videoFragmentListener.onVideoStart()
    }

    override fun onPlayerPause() {
        mBinding?.progressBarExoPlayerBuffering?.hide()
    }

    override fun onPlayerBuffering() {
        mBinding?.progressBarExoPlayerBuffering?.show()
    }

    private fun getAudioAttributes(): AudioAttributes =
            AudioAttributes.Builder().apply {
                setUsage(C.USAGE_MEDIA)
                setContentType(C.CONTENT_TYPE_SPEECH)
            }.build()

    override fun onDestroyView() {
        super.onDestroyView()
        sendEnagementTracking()
        appStateObserver?.dispose()
    }

    private fun sendEnagementTracking() {
        if (requireArguments().getBoolean("isFromFeed")) {
            var screenTime = (System.currentTimeMillis() - startTime) / 1000
            val event = StructuredEvent(EventConstants.CATEGORY_FEED, EventConstants.EVENT_NAME_FEED_POST_VIEW_VIDEO,
                    label = "ImagePagerScreen",
                    property = null,
                    value = screenTime.toDouble(),
                    eventParams = HashMap())
            if (!arguments?.getString(Constants.SOURCE).isNullOrEmpty()) {
                event.apply {
                    eventParams[Constants.SOURCE] = requireArguments().getString(Constants.SOURCE)!!
                }
            }
            analyticsPublisher?.publishEvent(event)
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSimpleVideoBinding {
        return FragmentSimpleVideoBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }

}
