package com.doubtnutapp.ui.forum.comments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.getColorRes
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.doubt.bookmark.data.entity.BookmarkResponse
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.videoPage.event.VideoPageEventManager

class CommentsRecyclerAdapter(
    val eventTracker: Tracker,
    private val videoPageEventManager: VideoPageEventManager,
    private val clickListener: (Comment, String) -> Unit,
    private val commentType: String,
    private val isReplies: Boolean = false,
    private val decorView: View? = null,
    private val analyticsPublisher: AnalyticsPublisher,
    private val assortmentId: String? = "",
    private val replyRecyclerViewVisible: Boolean = false,
    private val showBookmarkView: (BookmarkResponse) -> Unit = {}
) : RecyclerView.Adapter<CommentViewHolder>() {

    val commentsList = mutableListOf<Comment>()
    var source: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CommentViewHolder(
            itemView = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_comment,
                parent,
                false
            ),
            tracker = eventTracker,
            videoPageEventManager = videoPageEventManager,
            clickListener = clickListener,
            commentType = commentType,
            analyticsPublisher = analyticsPublisher,
            replyRecyclerViewVisible = replyRecyclerViewVisible
        )

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (isReplies && position == 0) {
            return VIEW_TYPE_PARENT_COMMENT
        }
        return VIEW_TYPE_COMMENT
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {

        if (commentsList.isEmpty()) return

        val comment = commentsList[position]

        holder.bind(comment, decorView, showBookmarkView, assortmentId)

        if (comment.isDoubt == "1" || comment.isDoubt == "true") {
            holder.binding.tvCommentMessage.backgroundTintList =
                ColorStateList.valueOf(holder.itemView.context.getColorRes(R.color.light_blue_comment))
        } else if (!comment.entityType.isNullOrEmpty() && comment.entityType?.contains("feed")) {
            holder.binding.tvCommentMessage.backgroundTintList =
                ColorStateList.valueOf(holder.itemView.context.getColorRes(R.color.blue_e0eaff))
        } else {
            holder.binding.tvCommentMessage.backgroundTintList =
                ColorStateList.valueOf(holder.itemView.context.getColorRes(R.color.seashell))
        }

        holder.binding.tvCommentMessage.text = comment.message

        holder.binding.tvCommentMessage.setOnLongClickListener {
            showPopUpMenu(it as TextView)
            true
        }

        holder.binding.ivProfileImage.setOnClickListener {
            val studentId = commentsList.getOrNull(position)?.studentId
            if (studentId != null) {
                FragmentWrapperActivity.userProfile(it.context, studentId, "comment")
            }
        }

        Linkify.addLinks(holder.binding.tvCommentMessage, Linkify.ALL)

        if (!comment.questionId.isNullOrBlank()) holder.binding.imageButtonCommentPlay.visibility =
            View.VISIBLE else holder.binding.imageButtonCommentPlay.visibility = View.GONE

        if (!isReplies && comment.entityId != null) {
            if (commentType == Constants.COMMENT_TYPE_DOUBT) {
                holder.binding.btnReply.hide()
            } else {
                holder.binding.btnReply.show()
            }
        } else {
            holder.binding.btnReply.hide()
            holder.binding.textViewReplies.hide()
        }

        if (isReplies) {
            when (getItemViewType(position)) {
                VIEW_TYPE_PARENT_COMMENT -> holder.binding.replyCommentSpace.hide()
                VIEW_TYPE_COMMENT -> holder.binding.replyCommentSpace.show()
            }
        }
    }

    fun updateList(list: List<Comment>) {
        val startingPosition = commentsList.size
        commentsList.addAll(list)
        notifyItemRangeInserted(startingPosition, list.size)
    }

    fun updateComment(comment: Comment) {
        val position = commentsList.indexOfFirst { it.id == comment.id }
        if (position != -1) {
            commentsList[position] = comment
        }
        notifyItemChanged(position)
    }

    fun addComment(comment: Comment) {
        commentsList.add(comment)
        notifyItemInserted(commentsList.size - 1)
    }

    fun clearList() {
        commentsList.clear()
        notifyDataSetChanged()
    }

    fun removeComment(comment: Comment) {
        val removePosition = commentsList.indexOf(comment)
        commentsList.remove(comment)
        notifyItemRemoved(removePosition)
    }

    private fun showPopUpMenu(anchor: TextView) {
        val popupMenu = PopupMenu(anchor.context, anchor)
        val menu = R.menu.menu_copytext
        popupMenu.inflate(menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            sendEvent(EventConstants.FEED_ENTITY_COPY_TEXT, anchor.context)
            sendEvent(EventConstants.FEED_ENTITY_COPY_TEXT_COMMENTS, anchor.context)
            copyText(anchor.context, anchor.text.toString())
            true
        }
    }

    private fun copyText(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("", text)
        clipboard?.setPrimaryClip(clip)
    }

    private fun sendEvent(eventName: String, context: Context) {
        (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
            .addStudentId(getStudentId())
            .addScreenName(EventConstants.PAGE_COMMENT_ACTIVITY)
            .track()

    }

    companion object {
        private const val VIEW_TYPE_PARENT_COMMENT = 1
        private const val VIEW_TYPE_COMMENT = 0
    }

}
