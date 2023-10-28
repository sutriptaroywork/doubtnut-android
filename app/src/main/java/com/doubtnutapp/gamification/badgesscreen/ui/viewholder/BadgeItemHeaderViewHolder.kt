package com.doubtnutapp.gamification.badgesscreen.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemBadgeHeaderBinding
import com.doubtnutapp.gamification.badgesscreen.model.BadgeHeaderViewType

class BadgeItemHeaderViewHolder(private val binding: ItemBadgeHeaderBinding) : BaseViewHolder<BadgeHeaderViewType>(binding.root) {

    override fun bind(data: BadgeHeaderViewType) {
        binding.badgeHeader = data
        binding.executePendingBindings()
    }

//    private fun sendEvent(eventName: String) {
//        val context = binding.root.context
//        context?.apply {
//            (context?.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
//                    .addNetworkState(NetworkUtils.isConnected(context!!).toString())
//                    .addStudentId(getStudentId())
//                    .track()
//        }
//    }
}