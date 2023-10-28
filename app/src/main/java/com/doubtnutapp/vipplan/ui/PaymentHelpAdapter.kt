package com.doubtnutapp.vipplan.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemPaymentHelp2Binding
import com.doubtnutapp.domain.payment.entities.PaymentHelpItem

class PaymentHelpAdapter()
    : RecyclerView.Adapter<BaseViewHolder<PaymentHelpItem>>() {

    val listings = mutableListOf<PaymentHelpItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<PaymentHelpItem> {
        return PaymentHelpViewHolder(
                ItemPaymentHelp2Binding
                        .inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int = listings.size

    override fun onBindViewHolder(holder: BaseViewHolder<PaymentHelpItem>, position: Int) {
        holder.bind(listings[position])
    }

    fun updateList(recentListings: List<PaymentHelpItem>) {
        listings.addAll(recentListings)
        notifyDataSetChanged()
    }

}