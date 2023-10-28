package com.doubtnutapp.gamification.myachievment.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.addEventNames
import com.doubtnutapp.base.OpenLeaderBoardActivity
import com.doubtnutapp.databinding.ItemProfileLeaderBoardBinding
import com.doubtnutapp.gamification.userProfileData.model.DailyLeaderboardItemDataModel
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId


class LeaderBoardViewHolder(var binding: ItemProfileLeaderBoardBinding, requiredWidth: Int, val actionsPerformer: ActionPerformer) : RecyclerView.ViewHolder(binding.root) {


    init {
        itemView.layoutParams.width = requiredWidth
    }

    fun bind(data: DailyLeaderboardItemDataModel) {
        binding.leaderboardItemDataModel = data
        binding.executePendingBindings()

            binding.clItemRoot.setOnClickListener {
                actionsPerformer.performAction(OpenLeaderBoardActivity)
                sendEvent(EventConstants.EVENT_NAME_OTHERS_PROFILE_CLICK)
            }
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