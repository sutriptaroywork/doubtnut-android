package com.doubtnutapp.live.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.View.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.core.utils.toast
import com.doubtnutapp.Constants
import com.doubtnutapp.base.Status
import com.doubtnutapp.databinding.FragmentLiveStreamPublishBinding
import com.doubtnutapp.hide
import com.doubtnutapp.live.viewmodel.LiveStreamViewModel
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.showToast
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.rtplibrary.rtmp.RtmpCamera1
import com.pedro.rtplibrary.util.BitrateAdapter
import dagger.android.support.AndroidSupportInjection
import net.ossrs.rtmp.ConnectCheckerRtmp


class LiveStreamPublishFragment :
    BaseBindingFragment<LiveStreamViewModel, FragmentLiveStreamPublishBinding>(),
    ConnectCheckerRtmp, TextureView.SurfaceTextureListener {

    private var rtmpCamera: RtmpCamera1? = null

    private lateinit var postId: String

    private var streamOverlayFragment: LiveOverlayFragment? = null

    private var isLive: Boolean = false

    private val mainHandler: Handler = Handler(Looper.getMainLooper())

    private var bitrateAdapter: BitrateAdapter? = null

    companion object {
        const val TAG = "LiveStreamPublishFragment"
        fun newInstance(livePostId: String): LiveStreamPublishFragment {
            return LiveStreamPublishFragment().apply {
                arguments = Bundle().apply { putString(Constants.POST_ID, livePostId) }
            }
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requireActivity().window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_HIDE_NAVIGATION or SYSTEM_UI_FLAG_FULLSCREEN or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        postId = requireArguments().getString(Constants.POST_ID) ?: return

        setClickListeners()
        setObservers()
        setupStreamOverlay()

        if (!hasPermission()) {
            requestPermission()
            return
        }

        confirmStreamStart()

        mBinding?.textureView?.surfaceTextureListener = this
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setObservers() {
        viewModel.liveStreamUrlLiveData.observe(viewLifecycleOwner, Observer {
            startPublishing(it)
        })
        viewModel.liveStreamEndLiveData.observe(viewLifecycleOwner, Observer {
            stopPublishing()
        })
        viewModel.stateLiveData.observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    hideStatus()
                }
                Status.NONE -> {
                    hideStatus()
                }
                Status.LOADING -> {
                    showStatus(it.message ?: "Starting live session")
                    mBinding?.btnStatus?.setOnClickListener(null)
                }
                Status.ERROR -> {
                    hideStatus()
                    showToast(activity, it.message!!)
                }
            }
        })
    }

    private fun setClickListeners() {
        mBinding?.btnSwitchCamera?.setOnClickListener {
            rtmpCamera?.switchCamera()
        }

        mBinding?.btnEnd?.setOnClickListener {
            confirmStreamEnd()
        }
    }

    private fun startPublishing(publisherUrl: String) {

        rtmpCamera = RtmpCamera1(mBinding?.textureView!!, this)
        val resolutionsFront = rtmpCamera!!.resolutionsFront
        if (rtmpCamera!!.prepareAudio()
                && rtmpCamera!!.prepareVideo(
                        resolutionsFront[0].width, resolutionsFront[0].height, 30, 1000 * 1024, false,
                        90
                )) {
            rtmpCamera!!.startStream(publisherUrl)
            rtmpCamera!!.switchCamera()
        } else {
            streamOverlayFragment?.handleStreamError("Unable to start encoders for live stream. " +
                    "We are unable to support live stream on this device yet.")
        }
        showStatus("Starting live session...")
    }

    private fun stopPublishing() {
        isLive = false
        rtmpCamera?.stopStream()
        streamOverlayFragment?.handleStreamEnd()
    }

    fun confirmStreamEnd(): Boolean {
        if (isLive) {
            AlertDialog.Builder(requireActivity())
                    .setTitle("End live session")
                    .setMessage("Are you sure you want to end the live session?")
                    .setPositiveButton("End") { _, _ ->
                        viewModel.endStream(postId)
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()
            return false
        }
        return true
    }

    private fun confirmStreamStart() {
        AlertDialog.Builder(requireActivity())
                .setTitle("Start live session")
                .setMessage("You are about to start a live session.")
                .setPositiveButton("● Go Live") { _, _ ->
                    viewModel.getStreamUrl(postId)
                    isLive = true
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    requireActivity().finish()
                }
                .setCancelable(false)
                .show()
    }

    private fun setupStreamOverlay() {
        streamOverlayFragment = LiveOverlayFragment.newInstance(postId)
        (requireActivity() as LiveActivity).addFragment(streamOverlayFragment!!)
    }

    private fun showStatus(message: String) {
        mBinding?.btnStatus?.show()
        mBinding?.btnStatus?.text = message
        mBinding?.progressBar?.show()
    }

    private fun hideStatus() {
        mBinding?.btnStatus?.hide()
        mBinding?.progressBar?.hide()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_CAMERA
                && grantResults.isNotEmpty()) {
            var valid = true
            for (grantResult in grantResults) {
                valid = valid && grantResult == PackageManager.PERMISSION_GRANTED
            }
            if (valid) {
                confirmStreamStart()
            } else {
                toast("Need access to camera and audio to start live stream")
                requireActivity().finish()
            }
        } else {
            toast("Need access to camera and audio to start live stream")
            requireActivity().finish()
        }

    }

    private fun hasPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(requireActivity(),
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        var permissions = arrayOf<String>()
        permissions += Manifest.permission.RECORD_AUDIO
        permissions += Manifest.permission.CAMERA

        requestPermissions(permissions, Constants.MY_PERMISSIONS_REQUEST_CAMERA)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onAuthErrorRtmp() {

    }

    override fun onAuthSuccessRtmp() {

    }

    override fun onConnectionFailedRtmp(p0: String) {
        isLive = false
        mainHandler.post {
            toast("Error starting live session")
            streamOverlayFragment?.handleStreamError("Failed to start live stream. Please try again.")
        }
    }

    override fun onConnectionSuccessRtmp() {
        isLive = true
        mainHandler.post {
            hideStatus()
            toast("Live session has started")
        }
        bitrateAdapter = BitrateAdapter {
            rtmpCamera?.setVideoBitrateOnFly(it)
        }
        bitrateAdapter!!.setMaxBitrate(rtmpCamera!!.bitrate)
    }

    override fun onDisconnectRtmp() {
        mainHandler.post {
            toast("Live session disconnected")
        }
    }

    override fun onNewBitrateRtmp(bitrate: Long) {
        bitrateAdapter?.adaptBitrate(bitrate)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, p1: Int, p2: Int) {
        rtmpCamera?.startPreview(CameraHelper.Facing.FRONT)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, p1: Int, p2: Int) {
        mBinding?.textureView?.setAspectRatio(480, 640)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        rtmpCamera?.stopPreview()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLiveStreamPublishBinding {
        return FragmentLiveStreamPublishBinding.inflate(inflater, container, false)
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