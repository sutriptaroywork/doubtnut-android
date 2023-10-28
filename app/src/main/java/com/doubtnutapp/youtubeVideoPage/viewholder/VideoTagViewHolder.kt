package com.doubtnutapp.youtubeVideoPage.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.VideoTagClick
import com.doubtnutapp.databinding.ItemVideoTagsBinding
import com.doubtnutapp.youtubeVideoPage.model.VideoTagViewItem

class VideoTagViewHolder(private val binding: ItemVideoTagsBinding): BaseViewHolder<VideoTagViewItem >(binding.root) {

    override fun bind(data: VideoTagViewItem) {
        binding.tag = data

        binding.tagLayout.setOnClickListener {
            actionPerformer?.performAction(
                    VideoTagClick(
                            data.title,
                            data.questionId
                    )
            )
        }
    }
}