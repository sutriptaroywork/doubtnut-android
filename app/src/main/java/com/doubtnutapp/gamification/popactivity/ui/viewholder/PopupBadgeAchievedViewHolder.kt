package com.doubtnutapp.gamification.popactivity.ui.viewholder

import android.view.View
import androidx.core.view.doOnPreDraw
import com.doubtnutapp.base.ViewNowClicked
import com.doubtnutapp.databinding.PopupviewBadgeachievedBinding
import com.doubtnutapp.gamification.popactivity.model.PopupBadgeAchieved

class PopupBadgeAchievedViewHolder(val binding: PopupviewBadgeachievedBinding) :
        PopViewHolder<PopupBadgeAchieved>(binding.root), View.OnClickListener {


    override fun bind(data: PopupBadgeAchieved) {
        super.bind(data)
        binding.badgeAchieved = data
        binding.shareWithFriendsButton.setOnClickListener(this)
        binding.root.doOnPreDraw {
            it.translationY = it.height.toFloat()
            it.animate().translationY(0f).duration = 200
        }
        binding.executePendingBindings()
    }

    override fun onClick(v: View?) {
        when (v) {

            // ToDo - Remove this according to design
//            binding.closePopup -> performAction(FinishActivity)

            // ToDo - Change action and also change in api for sharing message which is not coming right now
            binding.shareWithFriendsButton -> performAction(ViewNowClicked)
        }
    }
}