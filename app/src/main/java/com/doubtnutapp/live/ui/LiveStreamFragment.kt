package com.doubtnutapp.live.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.doubtnutapp.Constants
import com.doubtnutapp.databinding.FragmentLiveStreamBinding
import com.doubtnutapp.hide
import com.doubtnutapp.live.viewmodel.LiveStreamViewModel
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_HLS
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_RTMP
import com.doubtnutapp.utils.Utils
import com.google.android.exoplayer2.ExoPlaybackException
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.exo_simple_player_control_view.view.*
import kotlinx.android.synthetic.main.fragment_live_stream.view.*

class LiveStreamFragment : BaseBindingFragment<LiveStreamViewModel, FragmentLiveStreamBinding>(),
    ExoPlayerHelper.ExoPlayerStateListener,
    ExoPlayerHelper.MediaSourceStatusListener {

    companion object {
        const val TAG = "LiveStreamFragment"
        fun newInstance(livePostId: String, streamUrl: String, isVod: Boolean): LiveStreamFragment {
            return LiveStreamFragment().apply {
                arguments = Bundle().apply {
                    putString(Constants.POST_ID, livePostId)
                    putString(Constants.URL, streamUrl)
                    putBoolean(Constants.IS_VOD, isVod)
                }
            }
        }
    }

    private lateinit var postId: String
    private var isVod: Boolean = false

    private var exoPlayerHelper: ExoPlayerHelper? = null

    private var streamOverlayFragment: LiveOverlayFragment? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        postId = requireArguments().getString(Constants.POST_ID) ?: return
        isVod = requireArguments().getBoolean(Constants.IS_VOD)

        setupPlayer(requireArguments().getString(Constants.URL))

        setupStreamOverlay()
    }

    private fun setupPlayer(streamUrl: String?) {
        if (streamUrl == null) return
        startPlayer(streamUrl)
    }

    private fun setupStreamOverlay() {
        streamOverlayFragment = LiveOverlayFragment.newInstance(postId)
        (requireActivity() as LiveActivity).addFragment(streamOverlayFragment!!)
    }

    private fun startPlayer(streamUrl: String) {
        exoPlayerHelper = ExoPlayerHelper(requireActivity(), mBinding?.playerView!!)
        lifecycle.addObserver(exoPlayerHelper!!)
        if (isVod) {
            exoPlayerHelper!!.setMediaData(MEDIA_TYPE_HLS)
            mBinding?.playerView?.linearLayout3?.show()
            val marginParams =
                mBinding?.playerView?.linearLayout3?.layoutParams as ViewGroup.MarginLayoutParams
            marginParams.bottomMargin = Utils.convertDpToPixel(60f).toInt()
        } else {
            exoPlayerHelper!!.setMediaData(MEDIA_TYPE_RTMP)
            mBinding?.playerView?.linearLayout3?.hide()
        }
        exoPlayerHelper!!.setVideoUrl(streamUrl)
        exoPlayerHelper!!.setFallbackUrl(streamUrl)
        exoPlayerHelper!!.setExoPlayerStateListener(this)
        exoPlayerHelper!!.setMediaSourceStatusListener(this)
        mBinding?.playerView?.exo_fullscreen?.hide()
    }

    override fun onStart() {
        viewModel.viewerJoined(postId)
        super.onStart()
    }

    override fun onStop() {
        viewModel.viewerLeft(postId)
        super.onStop()
    }

    override fun onPlayerStart() {
        mBinding?.playerView?.bufferingProgressBar?.hide()
    }

    override fun onPlayerBuffering() {
        mBinding?.playerView?.bufferingProgressBar?.show()
    }

    override fun onPlayerEnd() {
        streamOverlayFragment?.handleStreamEnd()
    }

    override fun onPlayerPause() {

    }

    override fun onMediaSourceSelected(mediaSourceType: ExoPlayerHelper.MediaSourceType) {

    }

    override fun onMediaSourceFailed(
        mediaSourceType: ExoPlayerHelper.MediaSourceType,
        error: ExoPlaybackException?, fromFallbackHandler: Boolean,
        hlsTimeoutTime: Long, videoUrl: String
    ) {
        streamOverlayFragment?.handleStreamError("Error playing video. Please try again.")
    }

    override fun hasToShowDateDialog() {

    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLiveStreamBinding {
        return FragmentLiveStreamBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): LiveStreamViewModel {
        return ViewModelProviders.of(this, viewModelFactory).get(LiveStreamViewModel::class.java)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }
}