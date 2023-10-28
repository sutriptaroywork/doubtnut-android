package com.doubtnutapp.reward.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemRewardTermsBinding

class RewardTermsViewHolder(itemView: View) : BaseViewHolder<String>(itemView) {

    val binding = ItemRewardTermsBinding.bind(itemView)

    override fun bind(data: String) {
        binding.rewardTermNo.text = (adapterPosition + 1).toString() + "."
        binding.tvRewardTermsRule.text = data
    }
}