package com.doubtnutapp.store.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.store.model.BaseStoreViewItem
import com.doubtnutapp.store.ui.viewholder.StoreViewHolderFactory

class StoreResultListAdapter(private val actionsPerformer: ActionPerformer?) : RecyclerView.Adapter<BaseViewHolder<BaseStoreViewItem>>() {

    private val viewHolderFactory: StoreViewHolderFactory = StoreViewHolderFactory()
    private val storeResult = mutableListOf<BaseStoreViewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseStoreViewItem> {
        return (viewHolderFactory.getViewHolderFor(parent, viewType) as BaseViewHolder<BaseStoreViewItem>).apply {
            actionPerformer = this@StoreResultListAdapter.actionsPerformer
        }
    }

    override fun getItemCount(): Int = storeResult.size

    override fun getItemViewType(position: Int): Int {
        return storeResult[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<BaseStoreViewItem>, position: Int) = holder.bind(storeResult[position])

    fun updateFeeds(recentFeeds: List<BaseStoreViewItem>) {
        storeResult.clear()
        storeResult.addAll(recentFeeds)
        notifyDataSetChanged()
    }
}