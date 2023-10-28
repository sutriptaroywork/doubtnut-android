package com.doubtnutapp.reward.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.reward.KnowMoreListItem
import com.doubtnutapp.databinding.ItemRewardKnowMoreBinding

class KnowMoreRewardViewHolder(itemView: View) : BaseViewHolder<KnowMoreListItem>(itemView) {

    val binding = ItemRewardKnowMoreBinding.bind(itemView)

    override fun bind(data: KnowMoreListItem) {
        binding.tvLevelNumber.text = (bindingAdapterPosition + 1).toString()
        binding.tvLevelTitle.text = data.title
        binding.tvLevelDescription.text = data.description
    }
}