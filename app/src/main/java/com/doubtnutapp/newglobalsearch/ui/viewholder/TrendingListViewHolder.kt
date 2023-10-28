package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.ItemTrendingSearchBinding
import com.doubtnutapp.newglobalsearch.model.TrendingAndRecentFeedViewItem

class TrendingListViewHolder(val binding: ItemTrendingSearchBinding, val resultCount: Int) :
        BaseViewHolder<TrendingAndRecentFeedViewItem>(binding.root) {

    override fun bind(data: TrendingAndRecentFeedViewItem) {
        binding.trendingRecentData = data
        binding.root.setOnClickListener {
            if (data.type == "book" || data.type == "live_class_course" || data.type == "popular_on_doubtnut") {
                performAction(TrendingBookClicked(data, adapterPosition,resultCount))
            } else {
                performAction(TrendingPlaylistMongoEvent(data, adapterPosition, resultCount, false))
                performAction(TrendingPlaylistClicked(data.type, data.search, adapterPosition))
                performAction(NewTrendingSearchClicked(data.search, adapterPosition))
                performAction(TrendingRecentSearchItemClicked(data.search))
            }
        }
    }
}