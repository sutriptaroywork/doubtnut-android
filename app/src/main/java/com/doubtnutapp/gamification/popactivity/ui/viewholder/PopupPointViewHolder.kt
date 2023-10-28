package com.doubtnutapp.gamification.popactivity.ui.viewholder

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.LifecycleObserver
import com.doubtnutapp.databinding.PopupviewGamificationPointBinding
import com.doubtnutapp.gamification.popactivity.model.PopupPointAchieved



class PopupPointViewHolder(val binding: PopupviewGamificationPointBinding) : PopViewHolder<PopupPointAchieved>(binding.root), LifecycleObserver {
    override fun bind(data: PopupPointAchieved) {
        super.bind(data)
        binding.badgePoints = data
        adjustPointBadgeViewGravity(data.gravity)
        binding.root.doOnPreDraw {
            it.translationY = it.width.toFloat()
            it.animate().translationX(0f).duration = 200
        }

        setPointsEarned(data.message, binding.claimedPoints)
        binding.executePendingBindings()

    }

    private fun setPointsEarned(message: String, textView: TextView) {

        val startDigitIndex = message.indexOfFirst {
            it.isDigit()
        }
        val lastDigitIndex = message.indexOfLast {
            it.isDigit()
        }
        val spannableString = SpannableStringBuilder(message)

        if (startDigitIndex == -1 || lastDigitIndex == -1) {
            textView.text = message
        } else {
            spannableString.setSpan(
                    RelativeSizeSpan(2f),
                    startDigitIndex, lastDigitIndex + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            spannableString.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startDigitIndex, lastDigitIndex + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            textView.setText(spannableString, TextView.BufferType.SPANNABLE)
        }
    }


    private fun adjustPointBadgeViewGravity(gravity: Int) {
        val gravityForPointBadge = if (gravity and Gravity.START == Gravity.START) {
            Gravity.START
        } else if (gravity and Gravity.END == Gravity.END) {
            Gravity.END
        } else {
            Gravity.CENTER
        }

        (binding.popUpGamificationPointParentLayout.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.END
    }
}