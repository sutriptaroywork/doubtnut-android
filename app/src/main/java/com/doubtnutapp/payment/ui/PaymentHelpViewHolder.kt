package com.doubtnutapp.payment.ui

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemPaymentHelpBinding
import com.doubtnutapp.domain.payment.entities.PaymentItem

/**
 * Created by Anand Gaurav on 2019-12-20.
 */
class PaymentHelpViewHolder(val binding: ItemPaymentHelpBinding) : BaseViewHolder<PaymentItem>(binding.root) {

    override fun bind(data: PaymentItem) {
        binding.textViewTitle.text = data.name.orEmpty()
        binding.textViewDescription.text = data.value.orEmpty()
        binding.root.setOnClickListener {
            if (binding.textViewDescription.maxLines <= 0 || binding.textViewDescription.lineCount <= 0) {
                binding.textViewDescription.maxLines = 10
            } else {
                binding.textViewDescription.maxLines = 0
            }
        }
    }

}