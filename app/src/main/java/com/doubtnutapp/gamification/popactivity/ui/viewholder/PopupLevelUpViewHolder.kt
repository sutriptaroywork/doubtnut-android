package com.doubtnutapp.gamification.popactivity.ui.viewholder

import android.view.View
import androidx.core.view.doOnPreDraw
import com.doubtnutapp.base.OpenBadgesActivity
import com.doubtnutapp.databinding.PopupviewLevelupBinding
import com.doubtnutapp.gamification.popactivity.model.PopUpLevelUp

class PopupLevelUpViewHolder(val binding: PopupviewLevelupBinding) : PopViewHolder<PopUpLevelUp>(binding.root), View.OnClickListener {

    override fun bind(data: PopUpLevelUp) {
        super.bind(data)
        binding.badgeLevelUp = data
        binding.root.doOnPreDraw {
            it.translationY = it.height.toFloat()
            it.animate().translationY(0f).duration = 200
        }

        binding.shareWithFriendsButton.setOnClickListener(this)
        binding.executePendingBindings()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.shareWithFriendsButton -> {
                actionPerformer.performAction(
                        OpenBadgesActivity
                )
            }
        }
    }
}