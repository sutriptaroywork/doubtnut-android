package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.graphics.Color
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.doubtnutapp.Constants
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.ItemTrendingVideoFeedBinding
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.hide
import com.doubtnutapp.newglobalsearch.model.TrendingVideoViewItem
import com.doubtnutapp.show

class TrendingVideoListViewHolder(val binding: ItemTrendingVideoFeedBinding, val resultCount: Int) :
        BaseViewHolder<TrendingVideoViewItem>(binding.root) {

    override fun bind(data: TrendingVideoViewItem) {
        binding.videoFeed = data
        binding.ivVideo.text = data.ocrText
        binding.ivVideoThumb.isVisible = data.imageUrl.isNotEmpty()
        binding.ivVideo.isInvisible = binding.ivVideoThumb.isVisible
        setBackgroundColor(data.bgColor)
        binding.root.setOnClickListener {
            if(!data.deeplinkUrl.isNullOrEmpty()){
                performAction(TrendingCourseClicked(data))
            } else {
                performAction(PlayVideo(data.questionId, Constants.PAGE_SEARCH_SRP,
                    "", "", SOLUTION_RESOURCE_TYPE_VIDEO))
                performAction(getAction(data))
            }
        }

        if(data.questionId == "0"){
            binding.tvQuestioId.hide()
            binding.ibVideoPlay.hide()
        } else {
            binding.tvQuestioId.show()
            binding.ibVideoPlay.show()
        }
    }

    private fun setBackgroundColor(bgColor: String) {
        if (bgColor.isEmpty())
            return
        binding.ivVideo.setBackgroundColor(Color.parseColor(bgColor))
        binding.ivVideoThumb.setBackgroundColor(Color.parseColor(bgColor))
    }

    private fun getAction(data: TrendingVideoViewItem): Any {
        return when (data.type) {
            "recent_watched" -> NewTrendingRecentDoubtClicked(data, adapterPosition,resultCount)
            else -> NewTrendingMostWatchedClicked(data.ocrText, adapterPosition)
        }
    }
}