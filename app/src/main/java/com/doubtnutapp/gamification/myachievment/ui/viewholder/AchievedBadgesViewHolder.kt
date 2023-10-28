package com.doubtnutapp.gamification.myachievment.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.OpenBadgesActivity
import com.doubtnutapp.gamification.userProfileData.model.MyBadgesItemDataModel


class AchievedBadgesViewHolder(var binding: com.doubtnutapp.databinding.ItemBadgesBoardBinding, requiredWidth: Int, val actionsPerformer: ActionPerformer) : RecyclerView.ViewHolder(binding.root) {

    init {
        itemView.layoutParams.width = requiredWidth
    }

    fun bind(data: MyBadgesItemDataModel) {
        binding.recentBadge = data
        binding.executePendingBindings()

        binding.badgeCl.setOnClickListener {
            actionsPerformer.performAction(OpenBadgesActivity)
        }
    }
}