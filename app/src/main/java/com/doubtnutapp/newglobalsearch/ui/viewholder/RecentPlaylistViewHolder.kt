package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.TrendingPlaylistClicked
import com.doubtnutapp.base.TrendingRecentSearchItemClicked
import com.doubtnutapp.databinding.ItemTrendingPlaylistBinding
import com.doubtnutapp.newglobalsearch.model.SearchPlaylistViewItem

class RecentPlaylistViewHolder(val binding: ItemTrendingPlaylistBinding) :
        BaseViewHolder<SearchPlaylistViewItem>(binding.root){

    override fun bind(data: SearchPlaylistViewItem) {
        binding.searchPlaylist = data

        binding.root.setOnClickListener {
            if(data.id.isEmpty()) {
                performAction(TrendingPlaylistClicked("history", data.display, adapterPosition))
                performAction(TrendingRecentSearchItemClicked(data.display))
            }
            else if(data.id.isNotEmpty()){
                performAction(TrendingPlaylistClicked("trending", data.display, adapterPosition))
                performAction(TrendingRecentSearchItemClicked(data.display))
            }
        }
    }
}