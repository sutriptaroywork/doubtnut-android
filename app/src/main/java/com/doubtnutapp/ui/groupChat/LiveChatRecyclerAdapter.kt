package com.doubtnutapp.ui.groupChat

import android.content.Context
import android.text.util.Linkify
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.data.remote.models.LiveChatModel
import com.doubtnutapp.utils.UserUtil.getStudentId
import kotlinx.android.synthetic.main.item_group_chat_receive.view.*

class LiveChatRecyclerAdapter(
    val context: Context,
    val eventTracker: Tracker,
    clickListener: (Comment) -> Unit
) : RecyclerView.Adapter<GroupChatViewHolder<LiveChatModel>>() {

    private val commentsList = mutableListOf<Comment>()
    private var isAdded: Boolean = false
    private val liveChatViewHolderFactory: LiveChatViewHolderFactory =
        LiveChatViewHolderFactory(clickListener)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupChatViewHolder<LiveChatModel> {

        return liveChatViewHolderFactory.getViewHolderFor(
            parent,
            viewType
        ) as GroupChatViewHolder<LiveChatModel>
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun getItemViewType(position: Int): Int {
        val chat = commentsList[position]
        return if (chat.studentId == getStudentId()) {
            2
        } else
            1
    }

    override fun onBindViewHolder(holder: GroupChatViewHolder<LiveChatModel>, position: Int) {
        holder.bind(commentsList[position])
        Linkify.addLinks(holder.itemView.constraintLayout.textViewMessage, Linkify.ALL)
    }

    fun deleteComment(comment: Comment) {
        val indexToRemove = commentsList.indexOf(comment)
        commentsList.remove(comment)
        notifyItemRemoved(indexToRemove)
    }

    fun updateList(list: List<Comment>) {
        if (isAdded && (commentsList.size != null || !commentsList.isEmpty())) {
            commentsList.removeAt(commentsList.size - 1)
            isAdded = false
        }

        val startingPosition = commentsList.size
        commentsList.addAll(list)
        notifyItemRangeInserted(startingPosition, list.size)
    }

    fun addComment(comment: Comment, isAdded: Boolean) {
        commentsList.add(comment)
        this.isAdded = isAdded
        notifyItemInserted(commentsList.size - 1)
    }

}
