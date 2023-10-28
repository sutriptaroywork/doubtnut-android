package com.doubtnutapp.payment.ui

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemBreakThroughBinding
import com.doubtnutapp.domain.payment.entities.BreakThrough

/**
 * Created by Anand Gaurav on 2020-01-22.
 */
class BreakThroughViewHolder(val binding: ItemBreakThroughBinding) : BaseViewHolder<BreakThrough>(binding.root) {

    override fun bind(data: BreakThrough) {
        binding.breakThrough = data
        binding.executePendingBindings()
        binding.textViewBreakThroughTitle.text = data.title.orEmpty()
        binding.textViewBreakThroughTitleTime.text = data.date.orEmpty()
        binding.textViewBreakThroughTitleTimeDays.text = data.days.orEmpty()
    }

}