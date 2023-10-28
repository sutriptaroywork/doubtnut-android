package com.doubtnutapp.gamification.dailyattendance.ui.viewholder

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.OpenDailyStreakPage
import com.doubtnutapp.databinding.ItemCurrentStreakBinding
import com.doubtnutapp.gamification.dailyattendance.model.DailyAttendanceDataModel
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId

class CurrentStreakViewHolder(var binding: ItemCurrentStreakBinding, requiredWidth: Int)
    : BaseViewHolder<DailyAttendanceDataModel.CurrentStreakDataModel>(binding.root) {

    companion object {
        const val BADGE_TYPE_BADGE = "BADGE"
        const val BADGE_TYPE_NORMAL = "NONBADGE"
    }

    init {
        itemView.layoutParams.width = requiredWidth
    }

    override fun bind(data: DailyAttendanceDataModel.CurrentStreakDataModel) {
        binding.currentStreak = data

        when {
            data.type == BADGE_TYPE_BADGE -> {
                binding.pointsLayout.background = binding.root.resources.getDrawable(R.drawable.ic_daily_attendence_bg_yellow)
                binding.textView21.setTextColor(binding.root.resources.getColor(R.color.color_badge))
                binding.textView23.setTextColor(binding.root.resources.getColor(R.color.color_badge))

            }
            data.isAchieved == 1 -> {
                binding.pointsLayout.background = binding.root.resources.getDrawable(R.drawable.ic_daily_attendence_bg_green)
                binding.textView21.setTextColor(binding.root.resources.getColor(R.color.white))
                binding.textView23.setTextColor(binding.root.resources.getColor(R.color.white))
                binding.textView15.setTextColor(binding.root.resources.getColor(R.color.item_current_streak_day_enable_color))
            }
            else -> {
                binding.pointsLayout.background = binding.root.resources.getDrawable(R.drawable.ic_daily_attendence_bg_grey)
                binding.textView21.setTextColor(binding.root.resources.getColor(R.color.grey))
                binding.textView23.setTextColor(binding.root.resources.getColor(R.color.grey))
                binding.textView15.setTextColor(binding.root.resources.getColor(R.color.item_current_streak_day_disable_color))
            }
        }

        binding.root.setOnClickListener {
            performAction(OpenDailyStreakPage)
            sendEvent(EventConstants.EVENT_NAME_DAILY_STREAK_CLICK)
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


