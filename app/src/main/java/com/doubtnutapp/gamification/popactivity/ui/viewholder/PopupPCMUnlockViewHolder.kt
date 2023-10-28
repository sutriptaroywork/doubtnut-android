package com.doubtnutapp.gamification.popactivity.ui.viewholder

import android.view.View
import androidx.lifecycle.LifecycleObserver
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.addEventNames
import com.doubtnutapp.base.FinishActivity
import com.doubtnutapp.base.OpenTopicPage
import com.doubtnutapp.databinding.PopupviewPcmunlockBinding
import com.doubtnutapp.domain.pcmunlockpopup.BadgeRequiredType.PC_BADGE_UNLOCK_REACH_CONGRATULATION
import com.doubtnutapp.domain.pcmunlockpopup.BadgeRequiredType.POPUP_PC_UNLOCK_CLICK
import com.doubtnutapp.eventmanager.CommonEventManager
import com.doubtnutapp.gamification.popactivity.model.PopupUnlock
import com.doubtnutapp.utils.UserUtil.getStudentId

class PopupPCMUnlockViewHolder(val binding: PopupviewPcmunlockBinding, val commonEventManager: CommonEventManager) : PopViewHolder<PopupUnlock>(binding.root), LifecycleObserver, View.OnClickListener {
    override fun bind(data: PopupUnlock) {
        super.bind(data)
        binding.popUnlock = data
        binding.closePopup.setOnClickListener(this)
        binding.exploreNowButton.setOnClickListener(this)
        binding.executePendingBindings()
        sendEvent(PC_BADGE_UNLOCK_REACH_CONGRATULATION)
    }


    override fun onClick(v: View) {
        when (v) {
            binding.closePopup -> performAction(FinishActivity)

            binding.exploreNowButton -> binding.popUnlock?.actionData?.let {
                performAction(OpenTopicPage(
                        it.id,
                        it.title,
                        0,
                        ""))
                sendEvent(POPUP_PC_UNLOCK_CLICK)
            }
        }
    }


    fun sendEvent(eventName: String) {
        commonEventManager.eventWith(eventName)
        val context = binding.root.context
        (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addStudentId(getStudentId())
                .track()
    }


}