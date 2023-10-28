package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.view.View
import androidx.core.view.isVisible
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.topicboostergame2.LevelGameNumber
import com.doubtnutapp.databinding.ItemTopicBoosterGameLevelGameNumberBinding

/**
 * Created by devansh on 14/06/21.
 */

class LevelGameNumberViewHolder(itemView: View) : BaseViewHolder<LevelGameNumber>(itemView) {

    private val binding = ItemTopicBoosterGameLevelGameNumberBinding.bind(itemView)

    override fun bind(data: LevelGameNumber) {
        with(binding) {
            ivTick.isVisible = data.isDone
            tvQuestionNumber.isVisible = data.isDone.not()
            tvQuestionNumber.text = data.gameNumber.toString()
            viewDivider.isVisible = data.isLast.not()
        }
    }
}