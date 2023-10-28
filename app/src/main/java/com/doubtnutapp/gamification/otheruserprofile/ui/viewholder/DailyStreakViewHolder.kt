package com.doubtnutapp.gamification.otheruserprofile.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.gamification.otheruserprofile.model.DailyStreak

class DailyStreakViewHolder(itemView: View, requiredWidth: Int) : BaseViewHolder<DailyStreak>(itemView) {

    init {
        itemView.layoutParams.width = requiredWidth
    }

    override fun bind(data: DailyStreak) {
    }
}