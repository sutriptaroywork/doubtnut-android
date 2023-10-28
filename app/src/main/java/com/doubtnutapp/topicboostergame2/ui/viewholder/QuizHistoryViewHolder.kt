package com.doubtnutapp.topicboostergame2.ui.viewholder

import android.view.View
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.TbgQuizHistoryItemClicked
import com.doubtnutapp.base.extension.setBackgroundTint
import com.doubtnutapp.data.remote.models.topicboostergame2.QuizHistoryItem
import com.doubtnutapp.databinding.ItemTopicBoosterGameQuizHistoryBinding

/**
 * Created by devansh on 15/06/21.
 */

class QuizHistoryViewHolder(itemView: View) : BaseViewHolder<QuizHistoryItem>(itemView) {

    private val binding = ItemTopicBoosterGameQuizHistoryBinding.bind(itemView)

    override fun bind(data: QuizHistoryItem) {
        with(binding) {
            when (data.result) {
                0 -> rootLayout.setBackgroundTint(R.color.red_ffadad)
                1 -> rootLayout.setBackgroundTint(R.color.green_3bb54a)
                2 -> rootLayout.setBackgroundTint(R.color.yellow_ffc547)
            }
            tvResult.text = data.state
            tvTopic.text = data.title

            root.setOnClickListener {
                performAction(TbgQuizHistoryItemClicked(data.deeplink.orEmpty()))}
        }
    }
}