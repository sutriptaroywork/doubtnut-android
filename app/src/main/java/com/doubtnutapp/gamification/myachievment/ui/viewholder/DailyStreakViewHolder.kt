package com.doubtnutapp.gamification.myachievment.ui.viewholder

import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemProfileDailystreakBadgeBinding
import com.doubtnutapp.gamification.userProfileData.model.DailyAttendanceDataModel
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

class DailyStreakViewHolder(var binding: ItemProfileDailystreakBadgeBinding, requiredWidth: Int)
    : BaseViewHolder<DailyAttendanceDataModel>(binding.root) {

    init {
        itemView.layoutParams.width = requiredWidth
    }

    override fun bind(data: DailyAttendanceDataModel) {
    //    binding.dailyStreakBadge = data
//        binding.root.setOnClickListener {
//            performAction(OpenDailyStreakPage)
//            sendEvent(EventConstants.EVENT_NAME_DAILY_STREAK_CLICK)
//        }
        binding.executePendingBindings()

    }

    private fun sendEvent(eventName: String) {
        val context = binding.root.context
        context?.apply {
            (context?.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                    .addNetworkState(NetworkUtils.isConnected(context!!).toString())
                    .addStudentId(getStudentId())
                    .track()
        }
    }
}


