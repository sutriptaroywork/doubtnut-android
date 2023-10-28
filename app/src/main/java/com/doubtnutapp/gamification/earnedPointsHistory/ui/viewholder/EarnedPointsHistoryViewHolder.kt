package com.doubtnutapp.gamification.earnedPointsHistory.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemEarnedPointHistoryBinding
import com.doubtnutapp.gamification.earnedPointsHistory.model.EarnedPointsHistoryListDataModel

class EarnedPointsHistoryViewHolder(private val binding: ItemEarnedPointHistoryBinding) : BaseViewHolder<EarnedPointsHistoryListDataModel>(binding.root) {

    override fun bind(data: EarnedPointsHistoryListDataModel) {
        binding.earnedPointsHistoryItem = data
        binding.executePendingBindings()
    }
}