package com.doubtnutapp.store.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.store.model.BaseStoreViewItem
import com.doubtnutapp.store.ui.viewholder.StoreViewHolderFactory

class MyOrderListAdapter(private val actionsPerformer: ActionPerformer?) : RecyclerView.Adapter<BaseViewHolder<BaseStoreViewItem>>() {

    private val viewHolderFactory: StoreViewHolderFactory = StoreViewHolderFactory()
    private val myOrderList = mutableListOf<BaseStoreViewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseStoreViewItem> {
        return (viewHolderFactory.getViewHolderFor(parent, viewType) as BaseViewHolder<BaseStoreViewItem>).apply {
            actionPerformer = this@MyOrderListAdapter.actionsPerformer
        }
    }


    override fun getItemCount(): Int = myOrderList.size

    override fun getItemViewType(position: Int): Int {
        return myOrderList[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<BaseStoreViewItem>, position: Int) = holder.bind(myOrderList[position])

    fun updateFeeds(recentFeeds: List<BaseStoreViewItem>) {
        myOrderList.clear()
        myOrderList.addAll(recentFeeds)
        notifyDataSetChanged()
    }
}