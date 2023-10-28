package com.doubtnutapp.doubtpecharcha.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.LiveClassChatData
import com.doubtnutapp.doubtpecharcha.ui.adapter.DoubtPeCharchaChatAdapter

class DoubtPeCharchaViewHolderFactory {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): BaseViewHolder<LiveClassChatData> {
        return when (viewType) {
            DoubtPeCharchaChatAdapter.SENDER -> DoubtPeCharchaSenderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_doubt_pe_charcha_sender, parent, false)
            )

            DoubtPeCharchaChatAdapter.JOINED -> DoubtPeCharchaJoinedViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_user_joined, parent, false)
            )

            DoubtPeCharchaChatAdapter.RECEIVER -> DoubtPeCharchaReceiverViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_doubt_pe_charcha_receiver, parent, false)
            )

            else -> throw IllegalArgumentException()
        }
    }
}
