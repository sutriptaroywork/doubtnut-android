package com.doubtnutapp.store.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder

/**
 * Created by akshaynandwana on
 * 05, March, 2019
 **/
class StoreViewHolderFactory {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            R.layout.item_store_result_buy -> StoreItemBuyViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_store_result_buy, parent, false)
            )
            R.layout.item_store_result_open -> StoreItemOpenViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_store_result_open, parent, false)
            )
            R.layout.item_store_result_disabled -> StoreItemDisabledViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_store_result_disabled, parent, false)
            )
            R.layout.item_store_my_order -> MyOrderViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_store_my_order, parent, false)
            )

            else -> throw IllegalArgumentException()
        }
    }
}