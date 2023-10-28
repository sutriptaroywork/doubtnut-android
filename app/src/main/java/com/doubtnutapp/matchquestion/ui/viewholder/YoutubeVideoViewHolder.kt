package com.doubtnutapp.matchquestion.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.PlayYoutubeResult
import com.doubtnutapp.databinding.ItemYoutubeVideoBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.matchquestion.model.YoutubeViewItem

class YoutubeVideoViewHolder(val itemView: View) : BaseViewHolder<YoutubeViewItem>(itemView) {

    private val binding = ItemYoutubeVideoBinding.bind(itemView)

    override fun bind(data: YoutubeViewItem) {

        with(binding) {
            ivThumbnail.loadImage(data.thumbnail.high.url)
            tvTitle.text = data.description
            duration.text = data.duration

            layoutYoutubeResult.setOnClickListener {
                actionPerformer?.performAction(PlayYoutubeResult(data.youtubeId, position))
            }
        }
    }
}