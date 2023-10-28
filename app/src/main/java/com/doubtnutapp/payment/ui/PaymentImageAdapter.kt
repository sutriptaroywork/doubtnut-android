package com.doubtnutapp.payment.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder

/**
 * Created by Anand Gaurav on 2019-12-20.
 */
class PaymentImageAdapter : RecyclerView.Adapter<BaseViewHolder<String>>() {

    val listings = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<String> {
        return PaymentViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_payment_image, parent, false))
    }

    override fun getItemCount(): Int = listings.size

    override fun onBindViewHolder(holder: BaseViewHolder<String>, position: Int) {
        holder.bind(listings[position])
    }

    fun updateList(recentListings: List<String>) {
        listings.clear()
        listings.addAll(recentListings)
        notifyDataSetChanged()
    }


}