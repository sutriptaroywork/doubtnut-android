package com.doubtnutapp.revisioncorner.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RcResultHistoryWatchSolutionClick
import com.doubtnutapp.data.remote.models.revisioncorner.Result
import com.doubtnutapp.databinding.ItemRcResultHistoryBinding

class ResultHistoryViewHolder(itemView: View) : BaseViewHolder<Result>(itemView) {

    private val binding = ItemRcResultHistoryBinding.bind(itemView)

    override fun bind(data: Result) {
        with(binding) {
            tvTime.text = data.time
            tvMarks.text = data.marks
            tvTopic.text = data.topic
            ctaWatch.apply {
                text = data.ctaText
                setOnClickListener { performAction(RcResultHistoryWatchSolutionClick(data.deeplink)) }
            }
        }
    }
}