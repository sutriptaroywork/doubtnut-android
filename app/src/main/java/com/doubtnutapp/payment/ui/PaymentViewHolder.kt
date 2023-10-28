package com.doubtnutapp.payment.ui

import com.bumptech.glide.Glide
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemPaymentImageBinding

/**
 * Created by Anand Gaurav on 2019-12-20.
 */
class PaymentViewHolder(val binding: ItemPaymentImageBinding) : BaseViewHolder<String>(binding.root) {

    override fun bind(data: String) {
        Glide.with(binding.root.context).load(data).into(binding.imageViewPayment)
    }

}