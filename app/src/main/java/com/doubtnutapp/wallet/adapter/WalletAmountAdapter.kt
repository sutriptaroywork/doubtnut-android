package com.doubtnutapp.wallet.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemWalletAmountBinding
import com.doubtnutapp.databinding.ItemWalletAmountChildBinding
import com.doubtnutapp.domain.payment.entities.WalletAmount
import com.doubtnutapp.wallet.viewholder.WalletAmountChildViewHolder
import com.doubtnutapp.wallet.viewholder.WalletAmountViewHolder

/**
 * Created by devansh on 02/09/21.
 */

class WalletAmountAdapter(
    private val isForChildren: Boolean = false) : RecyclerView.Adapter<BaseViewHolder<WalletAmount>>() {

    companion object {
        const val PAYLOAD_HIDE_TOOLTIP = "hide_tooltip"

        private const val VIEW_TYPE_PARENT = 1
        private const val VIEW_TYPE_CHILD = 2
    }

    private val items = mutableListOf<WalletAmount>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<WalletAmount> =
        when (viewType) {
            VIEW_TYPE_PARENT -> {
                WalletAmountViewHolder(
                    ItemWalletAmountBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ).root
                )
            }
            VIEW_TYPE_CHILD -> {
                WalletAmountChildViewHolder(
                    ItemWalletAmountChildBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ).root
                )
            }
            else -> {
                throw IllegalArgumentException("Invalid view type: $viewType")
            }
        }

    override fun onBindViewHolder(holder: BaseViewHolder<WalletAmount>, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int =
        if (isForChildren) VIEW_TYPE_CHILD else VIEW_TYPE_PARENT

    override fun onBindViewHolder(
        holder: BaseViewHolder<WalletAmount>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            holder.bindItemPayload(payloads)
        }
    }

    fun hideToolTips() {
        notifyItemRangeChanged(0, itemCount, PAYLOAD_HIDE_TOOLTIP)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(items: List<WalletAmount>?) {
        this.items.clear()
        this.items.addAll(items.orEmpty())
        notifyDataSetChanged()
    }
}