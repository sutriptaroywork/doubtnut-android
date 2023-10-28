package com.doubtnutapp.gamification.badgesscreen.ui.viewholder

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenBadgeProgressDialog
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.databinding.ItemBadgeBinding
import com.doubtnutapp.gamification.badgesscreen.model.Badge
import com.doubtnutapp.sharing.GAMIFICATION_CHANNEL
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

class BadgeItemViewHolder(private val binding: ItemBadgeBinding) : BaseViewHolder<Badge>(binding.root) {
    override fun bind(data: Badge) {
        binding.badge = data

        binding.badgeShare.setOnClickListener {
            performAction(ShareOnWhatApp(
                    GAMIFICATION_CHANNEL,
                    data.featureType,
                    data.imageUrl,
                    null,
                    sharingMessage = data.sharingMessage,
                    questionId = ""

            ))
            sendEvent(EventConstants.EVENT_NAME_BADGE_SHARE_CLICKED)
        }

        binding.cardViewBadges.setOnClickListener {
            if (data.isOwn) {
                performAction(OpenBadgeProgressDialog(data.id.toString(), data.description, if (data.isAchieved) data.imageUrl else data.blurImage, data.featureType, data.sharingMessage, data.actionPage))
            }
        }

        binding.executePendingBindings()
    }


    private fun sendEvent(eventName: String) {
        val context = binding.root.context
        context?.apply {
            (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(context).toString())
                    .addStudentId(getStudentId())
                    .track()
        }
    }
}