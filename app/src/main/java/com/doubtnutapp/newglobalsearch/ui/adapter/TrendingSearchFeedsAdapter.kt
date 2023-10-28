package com.doubtnutapp.newglobalsearch.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.newglobalsearch.model.TrendingSearchDataListViewItem
import com.doubtnutapp.newglobalsearch.ui.viewholder.SearchResultViewHolderFactory


class TrendingSearchFeedsAdapter(private val actionPerformer: ActionPerformer?)
    : RecyclerView.Adapter<BaseViewHolder<TrendingSearchDataListViewItem>>() {

    private val feeds = mutableListOf<TrendingSearchDataListViewItem>()
    private val viewHolderFactory = SearchResultViewHolderFactory()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<TrendingSearchDataListViewItem> {
        return (viewHolderFactory.getViewHolderFor(parent, viewType, null, feeds.size) as BaseViewHolder<TrendingSearchDataListViewItem>).apply {
            actionPerformer = this@TrendingSearchFeedsAdapter.actionPerformer
        }
    }

    override fun getItemCount(): Int = feeds.size

    override fun getItemViewType(position: Int): Int {
        return feeds[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<TrendingSearchDataListViewItem>, position: Int) {
        feeds[position].position = position
        holder.bind(feeds[position])
    }

    fun updateFeeds(recentFeeds: List<TrendingSearchDataListViewItem>) {
        feeds.addAll(recentFeeds)
    }

}