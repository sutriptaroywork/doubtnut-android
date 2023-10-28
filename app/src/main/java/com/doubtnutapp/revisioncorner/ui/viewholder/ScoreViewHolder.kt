package com.doubtnutapp.revisioncorner.ui.viewholder

import android.view.View
import com.doubtnut.core.utils.applyBackgroundTint
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.data.remote.models.revisioncorner.Score
import com.doubtnutapp.databinding.ItemRevisionCornerScoreBinding

/**
 * Created by devansh on 17/08/21.
 */


class ScoreViewHolder(itemView: View) : BaseViewHolder<Score>(itemView) {

    private val binding = ItemRevisionCornerScoreBinding.bind(itemView)

    override fun bind(data: Score) {
        with(binding) {
            tvTitle.text = data.title
            tvScore.text = data.scoreText
            tvScore.applyBackgroundTint(data.backgroundColor)
        }
    }
}