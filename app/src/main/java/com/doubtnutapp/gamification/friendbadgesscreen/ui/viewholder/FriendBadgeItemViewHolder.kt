package com.doubtnutapp.gamification.friendbadgesscreen.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemBadgeBinding
import com.doubtnutapp.gamification.friendbadgesscreen.model.FriendBadge

class FriendBadgeItemViewHolder(private val binding: com.doubtnutapp.databinding.ItemFriendBadgeBinding) : BaseViewHolder<FriendBadge>(binding.root) {
    override fun bind(data: FriendBadge) {
        binding.badge = data
        binding.executePendingBindings()
    }
}