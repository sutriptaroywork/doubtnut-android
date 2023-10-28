package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.core.utils.toast
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeL
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.statusbarColor
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.VideoFragmentListener
import com.doubtnutapp.video.VideoPlayerManager
import com.doubtnutapp.videoPage.model.ViewAnswerData
import com.doubtnutapp.videoPage.viewmodel.VideoPageViewModel
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_video_dialog.*
import javax.inject.Inject

class VideoDialogActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "VideoDialogHolderActivity"
        const val ORIENTATION = "orientation"
        const val QUESTION_ID = "question_id"
        const val PAGE = "page"
        fun getStartIntent(
                context: Context,
                orientation: String,
                questionId: String,
                page: String,
        ) = Intent(context, VideoDialogActivity::class.java).apply {
            putExtra(ORIENTATION, orientation)
            putExtra(QUESTION_ID, questionId)
            putExtra(PAGE, page)
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var videoPageViewModel: VideoPageViewModel

    private var videoPlayerManager: VideoPlayerManager? = null

    var page = ""
    var questionId = ""
    var orientation = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.white_20)
        setContentView(R.layout.activity_video_dialog)
        imageViewClose.setOnClickListener {
            finish()
        }

        videoPageViewModel = ViewModelProvider(this, viewModelFactory).get(VideoPageViewModel::class.java)

        questionId = intent?.getStringExtra(QUESTION_ID).orEmpty()
        page = intent?.getStringExtra(PAGE).orEmpty()
        orientation = intent?.getStringExtra(ORIENTATION).orEmpty()

        videoPageViewModel.getVideoLiveData.observeL(this,
                ::onSuccess,
                ::onApiError,
                ::unAuthorizeUserError,
                ::ioExceptionHandler,
                ::updateProgressBarState)

        videoPageViewModel.viewVideo(questionId, null, null, page,
                "", "", null, false,
                false, null, null, false,
                false, false)
    }

    private fun onSuccess(viewAnswerData: ViewAnswerData) {
        videoPlayerManager = null
        viewAnswerData.resources.map { it.isPlayed = false }
        videoPlayerManager = VideoPlayerManager(supportFragmentManager,
                object : VideoFragmentListener {
                    override fun onPortraitRequested() {

                    }

                    override fun singleTapOnPlayerView() {
                        if (videoPlayerManager?.isPlayerControllerVisible == true) {
                            videoPlayerManager?.hidePlayerController()
                        } else {
                            videoPlayerManager?.showPlayerController()
                        }
                    }
                },
                R.id.videoContainer, { _, _ -> })

        videoPlayerManager?.setAndInitPlayFromResource(
                viewAnswerData.questionId,
                viewAnswerData.resources,
                viewAnswerData.viewId,
                0,
                false,
                page,
                calculateAspectRatio(orientation, viewAnswerData.aspectRatio),
                null, null, false, ocrText = viewAnswerData.ocrText)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {
        progressBar?.setVisibleState(state)
    }

    private fun ioExceptionHandler(e: Throwable) {
        val message = if (NetworkUtils.isConnected(this)) {
            getString(R.string.somethingWentWrong)
        } else {
            getString(R.string.string_noInternetConnection)
        }
        toast(message)
    }

    private fun calculateAspectRatio(orientation: String, aspectRatio: String?): String {
        val metrics = resources.displayMetrics
        return if (orientation == "portrait") {
            val ratio = metrics.widthPixels.toFloat() / metrics.heightPixels.toFloat()
            ratio.toString()
        } else if (orientation == "landscape") {
            val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
            ratio.toString()
        } else {
            if (aspectRatio.isNullOrBlank()) {
                VideoFragment.DEFAULT_ASPECT_RATIO
            } else {
                aspectRatio
            }
        }
    }

}