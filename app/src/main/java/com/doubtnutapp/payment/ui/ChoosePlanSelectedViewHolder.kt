package com.doubtnutapp.payment.ui

import android.graphics.Paint
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemChoosePlanSelectedBinding
import com.doubtnutapp.domain.payment.entities.DoubtPackageInfo
import com.doubtnutapp.utils.Utils

/**
 * Created by Anand Gaurav on 2020-01-21.
 */
class ChoosePlanSelectedViewHolder(val binding: ItemChoosePlanSelectedBinding) :
    BaseViewHolder<DoubtPackageInfo>(binding.root) {

    override fun bind(data: DoubtPackageInfo) {
        Utils.setWidthBasedOnPercentage(
            binding.root.context,
            binding.root,
            "3",
            R.dimen.spacing_plan
        )
        binding.transactionItem = data
        binding.executePendingBindings()

        binding.textViewOff.text = data.off
        binding.textViewDuration.text = data.duration
        binding.textViewOfferAmount.text = data.offerAmount.orEmpty()
        binding.textViewActualAmount.text = data.originalAmount.orEmpty()
        binding.textViewActualAmount.paintFlags =
            binding.textViewActualAmount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

}