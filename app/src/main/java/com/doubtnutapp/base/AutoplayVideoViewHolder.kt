package com.doubtnutapp.base

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.FragmentManager
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.model.Video
import com.doubtnutapp.resourcelisting.model.QuestionMetaDataModel
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.VideoFragmentListener
import com.doubtnutapp.video.VideoPlayerManager
import com.doubtnutapp.videoPage.ui.FullScreenVideoPageActivity
import com.doubtnutapp.videoPage.ui.VideoPageActivity

abstract class AutoplayVideoViewHolder<T : AutoplayRecyclerViewItem>(private val fm: FragmentManager?, rootView: View, val videoSource: String? = null, val playList: String = "") : BaseViewHolder<T>(rootView) {

    private var videoModel: Video? = null
    private var videoPlayerManager: VideoPlayerManager? = null
    private var ocr: String = ""

    override fun bind(data: T) {
        this.videoModel = if (data.videoObj != null &&
            data.videoObj!!.autoPlay &&
            data.videoObj?.videoResources.isNullOrEmpty().not()
        ) data.videoObj else null
        toggleShowAutoplayVideo(videoModel != null, data)
        if (data is QuestionMetaDataModel) {
            ocr = data.ocrText.orEmpty()
        }
        setObserver()
    }

    @SuppressLint("CheckResult")
    private fun setObserver() {
        DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            if (it is FullScreenVideoPageActivity.LastFullscreenDuration) {
                videoModel?.videoPosition = it.duration
                onStartAutoplay()
            }
        }
    }

    /*
    used for showing/hiding normal views and autoplay video related views.
    If showVideo is true, child viewholder need to hide all other irrelevenat views
     */
    abstract fun toggleShowAutoplayVideo(showVideo: Boolean, data: T)

    /*
    used to get the current page id for tracking meta
     */
    abstract fun getAutoplayVideoPage(): String

    open fun onStopAutoplay() {
        if (videoModel != null && fm != null &&
            videoPlayerManager?.videoFragment != null &&
            videoPlayerManager?.videoFragment!!.isAdded
        ) {
            // saving last position to resume from same place later on
            videoModel?.videoPosition = (videoPlayerManager?.currentPlayerPosition ?: 0).toLong()
            videoPlayerManager?.resetVideo()
            videoPlayerManager?.closeConvivaSession()
            videoPlayerManager = null
        }
    }

    open fun onStartAutoplay() {
        if (videoModel != null && fm != null && fm.isDestroyed.not()) {
            videoPlayerManager = null
            videoModel?.videoResources?.map { it.isPlayed = false }
            videoPlayerManager = VideoPlayerManager(
                fm, listener,
                R.id.videoContainer, { _, _ -> }
            )
            videoPlayerManager?.setAndInitPlayFromResource(
                videoModel?.questionId.orEmpty(),
                videoModel?.videoResources!!,
                videoModel?.viewId.orEmpty(),
                videoModel?.videoPosition ?: 0,
                false, getAutoplayVideoPage(),
                videoModel?.aspectRatio ?: VideoFragment.DEFAULT_ASPECT_RATIO,
                null, null,
                videoModel?.showFullScreen ?: false,
                viewHolder = true
            )
            sendEvent()
        }
    }

    private fun sendEvent() {
        ApxorUtils.logAppEvent(
            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
            Attributes().apply {
                putAttribute(EventConstants.SOURCE, getAutoplayVideoPage())
                putAttribute(EventConstants.QUESTION_ID, videoModel?.questionId.orEmpty())
            }
        )
    }

    private val listener = object : VideoFragmentListener {
        override fun onViewIdPublished(viewId: String) {
            videoModel?.viewId = viewId
        }

        override fun onFullscreenRequested() {
            if (videoModel == null) return
            if (Constants.PAGE_SEARCH_SRP.equals(videoSource, true)) {
                itemView.context.startActivity(
                    VideoPageActivity.startActivity(
                        itemView.context, questionId = videoModel!!.questionId!!, playlistId = playList, ocr = ocr, preLoadVideoData = videoModel!!.videoResources!![0]!!,
                        startPositionInSeconds = (
                            videoPlayerManager?.currentPlayerPosition
                                ?: 0
                            ).toLong(),
                        page = Constants.PAGE_SEARCH_SRP, isFullScreen = true
                    )
                )
            } else {
                val ongoingResource = videoPlayerManager?.ongoingVideoResource ?: return
                val videoModel = Video(
                    questionId = videoModel!!.questionId,
                    autoPlay = videoModel!!.autoPlay, viewId = videoModel!!.viewId,
                    videoResources = listOf(ongoingResource),
                    videoPosition = (videoPlayerManager?.currentPlayerPosition ?: 0).toLong(),
                    videoPage = getAutoplayVideoPage(),
                    showFullScreen = true,
                    aspectRatio = videoModel?.aspectRatio ?: VideoFragment.DEFAULT_ASPECT_RATIO
                )
                itemView.context.startActivity(
                    FullScreenVideoPageActivity.startActivity(itemView.context, videoModel)
                )
            }
        }

        override fun singleTapOnPlayerView() {
            if (videoPlayerManager?.isPlayerControllerVisible == true) {
                videoPlayerManager?.hidePlayerController()
            } else {
                videoPlayerManager?.showPlayerController()
            }
        }
    }
}
