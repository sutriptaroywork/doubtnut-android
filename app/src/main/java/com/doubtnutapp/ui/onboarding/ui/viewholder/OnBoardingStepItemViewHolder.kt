package com.doubtnutapp.ui.onboarding.ui.viewholder

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStepItem
import com.doubtnutapp.databinding.ItemOnboardingStepItemBinding
import com.doubtnutapp.hide
import com.doubtnutapp.show

/**
 * Created by Sachin Saxena on
 * 12, Feb, 2020
 **/
class OnBoardingStepItemViewHolder(
    val binding: ItemOnboardingStepItemBinding,
    private val stepItemClick: OnBoardingStepViewHolder.OnStepItemClick
) : BaseViewHolder<ApiOnBoardingStepItem>(binding.root) {

    override fun bind(data: ApiOnBoardingStepItem) {

        binding.title.text = data.title
        if (!data.subTitle.isNullOrEmpty()) {
            binding.subTitle.show()
            binding.subTitle.text = data.subTitle
         } else {
            binding.subTitle.hide()
        }

        if (data.rightArrow) {
            binding.title.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_arrow_down_grey_700,
                0
            )
        } else {
            binding.title.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                0,
                0
            )
        }

        if (data.isActive) {
            binding.title.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
            binding.subTitle.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.white
                )
            )
            binding.listItemCardView.setBackgroundResource(R.drawable.onboarding_step_item_selected)
            setTextViewDrawableColor(binding.title, R.color.white)
        } else {
            binding.title.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.color_incomplete
                )
            )
            binding.subTitle.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.onboarding_subtitle_inactive
                )
            )
            binding.listItemCardView.background = null
            binding.listItemCardView.setBackgroundResource(R.drawable.onboarding_step_item_unselected)
            setTextViewDrawableColor(binding.title, R.color.black)
        }

        binding.root.setOnClickListener {
            if (adapterPosition != -1) {
                stepItemClick.onStepItemClick(adapterPosition, data)
            }
        }

        binding.executePendingBindings()
    }

    private fun setTextViewDrawableColor(textView: TextView, color: Int) {
        for (drawable in textView.compoundDrawables) {
            drawable?.colorFilter =
                PorterDuffColorFilter(
                    ContextCompat.getColor(
                        binding.root.context,
                        color
                    ), PorterDuff.Mode.SRC_IN
                )
        }
    }
}