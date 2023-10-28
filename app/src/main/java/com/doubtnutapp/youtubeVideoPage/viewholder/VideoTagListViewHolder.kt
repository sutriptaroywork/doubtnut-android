package com.doubtnutapp.youtubeVideoPage.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemVideoTagsListBinding
import com.doubtnutapp.youtubeVideoPage.model.VideoTagListViewItem
import com.doubtnutapp.youtubeVideoPage.ui.adapter.VideoTagListAdapter

class VideoTagListViewHolder(private val binding: ItemVideoTagsListBinding): BaseViewHolder<VideoTagListViewItem >(binding.root) {

    override fun bind(data: VideoTagListViewItem) {

        val adapter = VideoTagListAdapter(actionPerformer)
        binding.tagRecyclerView.adapter = adapter
        adapter.updateFeeds(data.titleList)

    }
}