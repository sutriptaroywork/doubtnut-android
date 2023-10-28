package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.TbgEmojiClicked
import com.doubtnutapp.databinding.ItemTopicBoosterGameEmojiBinding

class DialogEmojiViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {

    private val binding = ItemTopicBoosterGameEmojiBinding.bind(itemView)

    override fun bind(data: String) {
        with(binding) {
            tvEmoji.text = data
            root.setOnClickListener {
                performAction(TbgEmojiClicked(data))
            }
        }
    }
}