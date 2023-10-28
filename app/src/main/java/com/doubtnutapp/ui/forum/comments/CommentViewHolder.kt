package com.doubtnutapp.ui.forum.comments

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.PlayAudioEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.ImageViewerActivity
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.databinding.ItemCommentBinding
import com.doubtnutapp.doubt.bookmark.data.entity.BookmarkResponse
import com.doubtnutapp.liveclass.ui.AudioPlayerActivity
import com.doubtnutapp.model.Video
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.videoPage.ui.FullScreenVideoPageActivity

class CommentViewHolder(
    itemView: ItemCommentBinding, val tracker: Tracker,
    private val videoPageEventManager: VideoPageEventManager,
    private val clickListener: (Comment, String) -> Unit,
    private val commentType: String,
    private val analyticsPublisher: AnalyticsPublisher,
    private val replyRecyclerViewVisible: Boolean
) : RecyclerView.ViewHolder(itemView.root) {

    val binding = itemView

    @SuppressLint("SetTextI18n")
    fun bind(
        comment: Comment,
        decorView: View? = null,
        showBookmarkView: (BookmarkResponse) -> Unit = {},
        assortmentId: String? = ""
    ) {
        binding.viewmodel = CommentItemViewModel(
            comment = comment,
            videoPageEventManager = videoPageEventManager,
            decorView = decorView,
            showBookmarkView = showBookmarkView,
            analyticsPublisher = analyticsPublisher,
            assortmentId = assortmentId
        )
        if (comment.entityId != null) {
            binding.overflowMenu.show()
            binding.overflowMenu.setOnClickListener {
                showMenu(it)
            }
        } else {
            binding.overflowMenu.hide()
        }

        binding.btnReply.setOnClickListener {
            clickListener(comment, "reply")
        }
        binding.imageViewComment.hide()
        binding.imageButtonCommentPlay.hide()
        binding.tvViewSolution.hide()

        when {
            comment.type == "top_doubt_answer_audio" && comment.resourceUrl.isNullOrBlank()
                .not() -> {
                binding.tvViewSolution.show()
                binding.tvViewSolution.setOnClickListener {
                    DoubtnutApp.INSTANCE.bus()?.send(PlayAudioEvent(true))
                    AudioPlayerActivity.getStartIntent(
                        binding.root.context,
                        comment.resourceUrl.orEmpty()
                    ).apply {
                        binding.root.context.startActivity(this)
                    }
                }
            }
            comment.type == "top_doubt_answer_text_image" -> {
                if (comment.resourceUrl.isNullOrBlank()) {
                    binding.imageViewComment.hide()
                } else {
                    binding.imageViewComment.show()
                    binding.imageViewComment.loadImageEtx(comment.resourceUrl.orEmpty())
                }
                binding.imageViewComment.setOnClickListener {
                    ImageViewerActivity.getStartIntent(
                        binding.root.context,
                        comment.resourceUrl.orEmpty()
                    )
                        .apply {
                            binding.root.context.startActivity(this)
                        }
                }
            }
            comment.type == "top_doubt_answer_video" -> {
                binding.imageViewComment.show()
                binding.imageViewComment.loadImageEtx(comment.resourceUrl.orEmpty())
                binding.imageButtonCommentPlay.show()
                listOf(binding.imageButtonCommentPlay, binding.imageViewComment).forEach {
                    it.setOnClickListener {
                        val videoObj = comment.videoObj ?: return@setOnClickListener
                        val video = Video(
                            videoObj.questionId,
                            true,
                            videoObj.viewId,
                            videoObj.resources?.map { videoResource ->
                                VideoResource(
                                    resource = videoResource.resource,
                                    drmScheme = videoResource.drmScheme,
                                    drmLicenseUrl = videoResource.drmLicenseUrl,
                                    mediaType = videoResource.mediaType,
                                    isPlayed = false,
                                    dropDownList = null,
                                    timeShiftResource = null,
                                    offset = videoResource.offset
                                )
                            },
                            0,
                            videoObj.page,
                            false,
                            VideoFragment.DEFAULT_ASPECT_RATIO
                        )
                        FullScreenVideoPageActivity.startActivity(
                            binding.root.context,
                            video,
                            true
                        )
                            .apply {
                                binding.root.context.startActivity(this)
                            }
                    }
                }
            }
            else -> {
                if (comment.image.isNullOrBlank()) {
                    binding.imageViewComment.hide()
                } else {
                    binding.imageViewComment.show()
                    binding.imageViewComment.loadImageEtx(comment.image.orEmpty())
                }
                binding.imageViewComment.setOnClickListener {
                    binding.viewmodel?.onCommentImageClicked(it)
                }
                if (!comment.questionId.isNullOrBlank()) binding.imageButtonCommentPlay.visibility =
                    View.VISIBLE else binding.imageButtonCommentPlay.visibility = View.GONE

                binding.imageButtonCommentPlay.setOnClickListener {
                    binding.viewmodel?.onCommentPlayImageClicked(it)
                }
            }
        }

        if (comment.isAdmin == true && comment.userTag?.isNotEmpty() == true) {
            binding.userTag.visibility = View.VISIBLE
            binding.userTag.text = comment.userTag
            binding.ivBulletImage.visibility = View.VISIBLE
        } else {
            binding.userTag.text = ""
            binding.userTag.visibility = View.GONE
            binding.ivBulletImage.visibility = View.GONE
        }

        if (commentType == Constants.COMMENT_TYPE_DOUBT && (comment.isDoubt == "1" || comment.isDoubt == "true")) {
            binding.textViewReplies.text =
                binding.root.context.getString(R.string.top_doubt_view_solution) + "(" + comment.replyCount.toString() + ")"
            if (comment.isBookmarked == null || replyRecyclerViewVisible) {
                binding.ivBookMark.hide()
            } else {
                binding.ivBookMark.show()
            }
        } else {
            binding.textViewReplies.text = comment.replyCount?.let {
                binding.root.context.resources.getQuantityString(
                    R.plurals.pl_reply,
                    it, it
                )
            }
            binding.ivBookMark.hide()
        }

        binding.textViewReplies.setOnClickListener {
            clickListener(comment, "reply_view")
        }

        binding.executePendingBindings()
    }

    private fun showMenu(anchor: View) {

        val comment = binding.viewmodel?.comment ?: return

        val layoutId =
            if (comment.isMyComment) R.layout.item_remove_comment else R.layout.item_report_comment

        val view = LayoutInflater.from(anchor.context).inflate(layoutId, null, true)
        val popupWindow = PopupWindow(view.context)
        popupWindow.contentView = view
        popupWindow.width = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.height = LinearLayout.LayoutParams.WRAP_CONTENT
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 20f
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.showAsDropDown(anchor)
        view.setOnClickListener {
            clickListener(comment, "popup_menu")
            popupWindow.dismiss()
        }
    }
}
