package com.doubtnutapp.newglobalsearch.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.SearchPlaylistClickedEvent
import com.doubtnutapp.base.YoutubeVideo
import com.doubtnutapp.databinding.ItemYoutubeResultsBinding
import com.doubtnutapp.newglobalsearch.model.YoutubeSearchViewItem
import com.google.gson.Gson

class YoutubeVideoListViewHolder(val binding: ItemYoutubeResultsBinding) :
        BaseViewHolder<YoutubeSearchViewItem>(binding.root) {

    override fun bind(data: YoutubeSearchViewItem) {
        binding.itemData = data

        binding.root.setOnClickListener {
            performAction(SearchPlaylistClickedEvent(Gson().toJson(data), data.title, data.videoId,
                "Youtube", data.fakeType, adapterPosition,assortmentId = ""))
            performAction(YoutubeVideo(data.videoId, data.title))
        }
    }
}