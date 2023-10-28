package com.doubtnutapp.dnr.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.dnr.model.VoucherInfoItem
import com.doubtnutapp.dnr.ui.viewholder.DnrVoucherInfoItemViewHolder

class DnrVoucherInfoItemAdapter : RecyclerView.Adapter<DnrVoucherInfoItemViewHolder>() {

    private val infoItems = mutableListOf<VoucherInfoItem>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DnrVoucherInfoItemViewHolder =
        DnrVoucherInfoItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_dnr_voucher_info, parent, false)
        )

    override fun onBindViewHolder(holder: DnrVoucherInfoItemViewHolder, position: Int) {
        holder.bind(infoItems[position])
    }

    override fun getItemCount() = infoItems.size

    fun updateItems(items: List<VoucherInfoItem>) {
        this.infoItems.clear()
        this.infoItems.addAll(items)
        notifyDataSetChanged()
    }
}
