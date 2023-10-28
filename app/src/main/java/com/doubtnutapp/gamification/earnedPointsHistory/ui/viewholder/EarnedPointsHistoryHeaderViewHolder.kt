package com.doubtnutapp.gamification.earnedPointsHistory.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemEarnedPointHistoryHeaderBinding
import com.doubtnutapp.gamification.earnedPointsHistory.model.EarnedPointsHistoryHeaderDataModel

class EarnedPointsHistoryHeaderViewHolder(private val binding: ItemEarnedPointHistoryHeaderBinding) : BaseViewHolder<EarnedPointsHistoryHeaderDataModel>(binding.root) {

    override fun bind(data: EarnedPointsHistoryHeaderDataModel) {
        binding.earnedPointsHistoryHeder = data
        binding.executePendingBindings()
    }
}