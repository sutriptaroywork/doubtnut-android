package com.doubtnutapp.gamification.popactivity.ui.viewholder

import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.LifecycleOwner
import com.doubtnutapp.base.FinishActivity
import com.doubtnutapp.databinding.PopupviewBadgeBinding
import com.doubtnutapp.gamification.popactivity.model.PopupBadge

class PopupBadgeViewHolder(val binding: PopupviewBadgeBinding) : PopViewHolder<PopupBadge>(binding.root), View.OnClickListener {

    override fun bind(data: PopupBadge) {
        super.bind(data)
        binding.badge = data
        binding.closePopup.setOnClickListener(this)
        binding.root.doOnPreDraw {
            it.translationY = it.height.toFloat()
            it.animate().translationY(0f).duration = 200
        }
        binding.executePendingBindings()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.closePopup -> performAction(FinishActivity)
        }
    }
}