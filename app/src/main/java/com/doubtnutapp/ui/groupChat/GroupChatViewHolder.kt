package com.doubtnutapp.ui.groupChat

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.data.remote.models.LiveChatModel

abstract class
GroupChatViewHolder<in T: LiveChatModel>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(feedModel: Comment)
}