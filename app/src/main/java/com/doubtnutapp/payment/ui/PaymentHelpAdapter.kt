package com.doubtnutapp.payment.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.domain.payment.entities.PaymentItem

/**
 * Created by Anand Gaurav on 2019-12-20.
 */
class PaymentHelpAdapter(private val actionPerformer: ActionPerformer) : RecyclerView.Adapter<BaseViewHolder<PaymentItem>>() {

    val listings = mutableListOf<PaymentItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<PaymentItem> {
        return PaymentHelpViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_payment_help, parent, false)).also {
            it.actionPerformer = this@PaymentHelpAdapter.actionPerformer
        }
    }

    override fun getItemCount(): Int = listings.size

    override fun onBindViewHolder(holder: BaseViewHolder<PaymentItem>, position: Int) {
        holder.bind(listings[position])
    }

    fun updateList(recentListings: List<PaymentItem>) {
        listings.clear()
        listings.addAll(recentListings)
        notifyDataSetChanged()
    }


}