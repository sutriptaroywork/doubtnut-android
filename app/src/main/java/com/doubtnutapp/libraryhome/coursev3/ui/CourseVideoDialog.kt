package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.doubtnutapp.R
import com.doubtnutapp.model.Video
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.VideoFragmentListener
import com.doubtnutapp.video.VideoPlayerManager
import kotlinx.android.synthetic.main.dialog_course_video.*

class CourseVideoDialog : DialogFragment() {

    private var videoModel: Video? = null
    private var videoPlayerManager: VideoPlayerManager? = null

    companion object {
        const val TAG: String = "VideoDialog"
        private const val VIDEO_DATA = "video_data"
        fun newInstance(video: Video): CourseVideoDialog {
            return CourseVideoDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(VIDEO_DATA, video)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_course_video, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        videoModel = arguments?.getParcelable(VIDEO_DATA)
        videoPlayerManager = null
        videoModel?.videoResources?.map { it.isPlayed = false }
        videoPlayerManager = VideoPlayerManager(childFragmentManager,
                object : VideoFragmentListener {
                    override fun onPortraitRequested() {

                    }

                    override fun onVideoCompleted() {
                        super.onVideoCompleted()
                        dismiss()
                    }

                    override fun singleTapOnPlayerView() {
                        if (videoPlayerManager?.isPlayerControllerVisible == true) {
                            videoPlayerManager?.hidePlayerController()
                        } else {
                            videoPlayerManager?.showPlayerController()
                        }
                    }
                },
                R.id.videoContainer,  { _, _ -> })

        videoPlayerManager?.setAndInitPlayFromResource(
                videoModel?.questionId.orEmpty(),
                videoModel?.videoResources!!,
                videoModel?.viewId.orEmpty(),
                videoModel?.videoPosition ?: 0,
                false,
                videoModel?.videoPage.orEmpty(),
                VideoFragment.DEFAULT_ASPECT_RATIO,
                null, null, false)

        imageViewClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        activity?.finish()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }

}