package com.doubtnutapp.ui.groupChat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.data.remote.models.LiveChatViewTypes

class LiveChatViewHolderFactory(private val clickListener: (Comment) -> Unit) {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): GroupChatViewHolder<*> {

        return when (viewType) {
            LiveChatViewTypes.LIVE_CHAT_RECEIVE -> LiveChatReceiveViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_group_chat_receive,
                    parent,
                    false
                ),
                clickListener
            )
            else -> LiveChatSendViewHolder(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_group_chat_send,
                    parent,
                    false
                )
            )
        }
    }

}
