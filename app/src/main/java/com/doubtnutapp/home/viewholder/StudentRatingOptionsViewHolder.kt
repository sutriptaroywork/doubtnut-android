package com.doubtnutapp.home.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RatingCheckboxClicked
import com.doubtnutapp.base.RatingCheckboxUnchecked
import com.doubtnutapp.databinding.ItemStudentRatingOptionsBinding

class StudentRatingOptionsViewHolder(val binding: ItemStudentRatingOptionsBinding) : BaseViewHolder<String>(binding.root) {

    override fun bind(data: String) {
        binding.ratingOptionsText.text = data

        binding.root.setOnClickListener {
            if(!binding.ratingOptionsCheckbox.isChecked) {
                binding.ratingOptionsCheckbox.isChecked = true
                performAction(RatingCheckboxClicked(data))
            } else {
                binding.ratingOptionsCheckbox.isChecked = false
                performAction(RatingCheckboxUnchecked(data))
            }
        }
    }
}