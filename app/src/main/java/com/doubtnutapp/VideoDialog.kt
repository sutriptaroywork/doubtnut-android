package com.doubtnutapp

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.databinding.DialogVideoBinding
import com.doubtnutapp.dialogHolder.DialogHolderActivity
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.VideoFragmentListener
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout

class VideoDialog :
    BaseBindingDialogFragment<DummyViewModel, DialogVideoBinding>(),
    VideoFragmentListener {

    lateinit var videoFragment: VideoFragment

    companion object {
        const val TAG: String = "VideoDialog"
        const val VIDEO_URL: String = "VIDEO_URL"
        const val VIDEO_ORIENTATION: String = "orientation"
        const val QUESTION_ID: String = "question_id"
        const val PAGE: String = "page"
        const val DRM_SCHEME = "drm_scheme"
        const val DRM_LICENSE_URL = "drm_license_url"
        const val MEDIA_TYPE = "media_type"
        fun newInstance(
            videoUrl: String,
            orientation: String,
            questionId: Int,
            page: String,
            drmScheme: String? = null,
            drmLicenseUrl: String? = null,
            mediaType: String? = null
        ): VideoDialog {
            return VideoDialog().apply {
                arguments = Bundle().apply {
                    putString(VIDEO_URL, videoUrl)
                    putString(VIDEO_ORIENTATION, orientation)
                    putInt(QUESTION_ID, questionId)
                    putString(PAGE, page)
                    putString(DRM_SCHEME, drmScheme)
                    putString(DRM_LICENSE_URL, drmLicenseUrl)
                    putString(MEDIA_TYPE, mediaType)
                }
            }
        }
    }

    private var videoUrl: String = ""
    private var orientation: String = ""
    private var questionId: Int = -1
    private var page: String = ""

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        videoUrl = arguments?.getString(VIDEO_URL, "") ?: ""
        orientation = arguments?.getString(VIDEO_ORIENTATION, "") ?: ""
        questionId = arguments?.getInt(QUESTION_ID, -1) ?: -1
        page = arguments?.getString(PAGE, "") ?: ""
        val drmScheme = arguments?.getString(DRM_SCHEME, "").orEmpty()
        val drmLicenseUrl = arguments?.getString(DRM_LICENSE_URL, "").orEmpty()
        val mediaType = arguments?.getString(MEDIA_TYPE, MEDIA_TYPE_BLOB)

        val qid = if (questionId == -1) {
            ""
        } else {
            questionId.toString()
        }

        val videoData = VideoFragment.Companion.VideoData(
            questionId = qid,
            videoUrl = videoUrl,
            fallbackVideoUrl = videoUrl,
            hlsTimeoutTime = 0,
            aspectRatio = calculateAspectRatio(orientation),
            autoPlay = false,
            show_fullscreen = true,
            page = page,
            isPlayFromBackStack = false,
            mediaType = mediaType,
            drmScheme = drmScheme,
            lockUnlockLogs = "",
            drmLicenseUrl = drmLicenseUrl
        )

        videoFragment = VideoFragment.newInstance(
            videoData,
            null,
            this,
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        )
        childFragmentManager.beginTransaction().replace(R.id.videoContainer, videoFragment).commit()
        mBinding?.imageViewClose?.setOnClickListener {
            dismiss()
        }
    }

    override fun onFullscreenRequested() {
    }

    override fun onPortraitRequested() {
    }

    override fun onVideoCompleted() {
    }

    override fun onViewIdPublished(viewId: String) {
    }

    override fun singleTapOnPlayerView() {
    }

    private fun calculateAspectRatio(orientation: String): String {
        val metrics = requireContext().resources.displayMetrics
        if (orientation == "portrait") {
            val ratio = metrics.widthPixels.toFloat() / metrics.heightPixels.toFloat()
            return ratio.toString()
        } else if (orientation == "landscape") {
            val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
            return ratio.toString()
        } else {
            return "16:9"
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (activity is DialogHolderActivity) {
            activity?.finish()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (activity is DialogHolderActivity) {
            activity?.finish()
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogVideoBinding {
        return DialogVideoBinding.inflate(inflater, container, false)
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
