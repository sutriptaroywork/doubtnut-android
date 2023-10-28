package com.doubtnutapp.doubtpecharcha.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.LiveClassChatData
import com.doubtnutapp.databinding.ItemChatUserJoinedBinding

class DoubtPeCharchaJoinedViewHolder(itemView: View) : BaseViewHolder<LiveClassChatData>(itemView) {

    val binding = ItemChatUserJoinedBinding.bind(itemView)

    override fun bind(data: LiveClassChatData) {
        binding.msgTv.text = data.message.orEmpty()
    }
}
