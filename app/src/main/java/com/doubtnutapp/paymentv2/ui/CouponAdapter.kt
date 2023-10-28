package com.doubtnutapp.paymentv2.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.CouponApplied
import com.doubtnutapp.databinding.CouponCodeItemBinding
import com.doubtnutapp.domain.payment.entities.CouponItem
import com.doubtnutapp.setVisibleState


/**
 * Created by Akshat Jindal on 2022-01-14.
 */

class CouponAdapter(val list: List<CouponItem>, val actionPerformer: ActionPerformer) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CouponViewHolder(
            CouponCodeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]
        (holder as CouponViewHolder).bind(data, actionPerformer)
    }

    override fun getItemCount() = list.size

    class CouponViewHolder(val binding: CouponCodeItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: CouponItem, actionPerformer: ActionPerformer) {
            binding.apply {

                tvCouponCode.text = data.title
                tvSaveText.text = data.amountSaved
                tvApplyCoupon.text = data.btnText
                tvValid.setVisibleState(data.validity.isNullOrEmpty().not())
                tvValid.text = data.validity

                root.setOnClickListener {
                    actionPerformer.performAction(CouponApplied(data.title.orEmpty()))
                }
            }
        }
    }
}