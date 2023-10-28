package com.doubtnutapp.gamification.gamepoints.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenPage
import com.doubtnutapp.databinding.ItemGamificationViewPointsInfoBinding
import com.doubtnutapp.gamification.gamepoints.model.ActionConfigDataItemDataModel

class GamePointsViewHolder(private val binding: ItemGamificationViewPointsInfoBinding) : BaseViewHolder<ActionConfigDataItemDataModel>(binding.root) {

    override fun bind(data: ActionConfigDataItemDataModel) {
        binding.actionConfigDataItemData = data
        binding.executePendingBindings()
        binding.cvItemGamePointsInfo.setOnClickListener { 
            performAction(OpenPage(data.actionPage))
        }
    }
}