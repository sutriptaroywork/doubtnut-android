package com.doubtnutapp.matchquestion.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.ShowMoreMatches
import com.doubtnutapp.databinding.ItemShowMoreYoutubeVideoBinding
import com.doubtnutapp.hide
import com.doubtnutapp.matchquestion.model.ShowMoreViewItem
import com.doubtnutapp.show

class ShowMoreYoutubeVideoViewHolder(val itemView: View) :
    BaseViewHolder<ShowMoreViewItem>(itemView) {

    private val binding = ItemShowMoreYoutubeVideoBinding.bind(itemView)

    companion object {
        const val HIDE_PROGRESS_BAR = 0
        const val SHOW_PROGRESS_BAR = 1
        const val NO_RESULT_FOUND = 2
    }

    override fun bind(data: ShowMoreViewItem) {

        with(binding) {
            when (data.status) {
                HIDE_PROGRESS_BAR -> {
                    progressYoutubeVideo.hide()
                    tvShowMore.show()
                    ivArrowDown.show()
                    tvNoResults.hide()
                }
                SHOW_PROGRESS_BAR -> {
                    progressYoutubeVideo.show()
                    tvShowMore.hide()
                    ivArrowDown.hide()
                    tvNoResults.hide()
                }
                NO_RESULT_FOUND -> {
                    progressYoutubeVideo.hide()
                    tvShowMore.hide()
                    ivArrowDown.hide()
                    tvNoResults.show()
                }
            }

            tvShowMore.setOnClickListener {
                actionPerformer?.performAction(ShowMoreMatches(absoluteAdapterPosition, data))
            }

            ivArrowDown.setOnClickListener {
                actionPerformer?.performAction(ShowMoreMatches(absoluteAdapterPosition, data))
            }
        }
    }
}