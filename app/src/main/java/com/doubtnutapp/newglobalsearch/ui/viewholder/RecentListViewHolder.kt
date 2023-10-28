package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.ItemRecentSearchBinding
import com.doubtnutapp.newglobalsearch.model.TrendingAndRecentFeedViewItem

class RecentListViewHolder(val binding: ItemRecentSearchBinding, val resultCount: Int) :
        BaseViewHolder<TrendingAndRecentFeedViewItem>(binding.root) {

    override fun bind(data: TrendingAndRecentFeedViewItem) {
        binding.trendingRecentData = data

        binding.root.setOnClickListener {
            performAction(TrendingPlaylistMongoEvent(data, adapterPosition, resultCount, true))
            performAction(TrendingPlaylistClicked("history", data.display, adapterPosition, true))
            performAction(NewRecentSearchClicked(data.display, adapterPosition))
            performAction(TrendingRecentSearchItemClicked(data.display, true))
        }
    }
}