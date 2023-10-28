package com.doubtnutapp.gamification.popactivity.ui.viewholder

import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.doubtnutapp.R
import com.doubtnutapp.eventmanager.CommonEventManager
import com.doubtnutapp.gamification.popactivity.model.GamificationPopup

class PopUpViewHolderFactory() {

    fun getViewHolderFor(
        context: Context,
        viewType: Int,
        commonEventManager: CommonEventManager
    ): PopViewHolder<GamificationPopup>? {
        return when (viewType) {
            R.layout.popupview_badgeachieved -> PopupBadgeAchievedViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(context), viewType, null, false)
            ) as PopViewHolder<GamificationPopup>
            R.layout.popupview_levelup -> PopupLevelUpViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(context), viewType, null, false)
            ) as PopViewHolder<GamificationPopup>
            R.layout.popupview_badge -> PopupBadgeViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(context), viewType, null, false)
            ) as PopViewHolder<GamificationPopup>
            R.layout.popupview_gamification_point -> PopupPointViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(context), viewType, null, false)
            ) as PopViewHolder<GamificationPopup>

            R.layout.popupview_pcmunlock -> PopupPCMUnlockViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(context), viewType, null, false),
                commonEventManager
            ) as PopViewHolder<GamificationPopup>

            R.layout.popupview_alert -> PopupAlertViewHolder(
                DataBindingUtil.inflate(LayoutInflater.from(context), viewType, null, false),
                commonEventManager
            ) as PopViewHolder<GamificationPopup>
            else -> null
        }
    }
}