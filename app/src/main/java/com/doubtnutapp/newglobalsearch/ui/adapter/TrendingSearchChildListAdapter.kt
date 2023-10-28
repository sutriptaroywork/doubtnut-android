package com.doubtnutapp.newglobalsearch.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.newglobalsearch.model.TrendingSearchFeedViewItem
import com.doubtnutapp.newglobalsearch.ui.viewholder.SearchResultViewHolderFactory

class TrendingSearchChildListAdapter(
        private val actionPerformer: ActionPerformer?) :
        RecyclerView.Adapter<BaseViewHolder<TrendingSearchFeedViewItem>>() {

    private val viewHolderFactory = SearchResultViewHolderFactory()

    private var dataList = listOf<TrendingSearchFeedViewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<TrendingSearchFeedViewItem> {
        return (viewHolderFactory.getViewHolderFor(parent, viewType,null,dataList.size) as BaseViewHolder<TrendingSearchFeedViewItem>).apply {
            actionPerformer = this@TrendingSearchChildListAdapter.actionPerformer
        }
    }

    override fun getItemViewType(position: Int): Int {
        return dataList[position].viewType
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: BaseViewHolder<TrendingSearchFeedViewItem>, position: Int) =
            holder.bind(dataList[position])

    fun updateData(data: List<TrendingSearchFeedViewItem>) {
        dataList = data
        notifyDataSetChanged()
    }
}