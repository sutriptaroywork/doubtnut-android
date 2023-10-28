package com.doubtnutapp.revisioncorner.ui.viewholder

import android.view.View
import androidx.core.view.isVisible
import com.doubtnut.core.utils.disableTouchEvents
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.RcStatsViewAllClick
import com.doubtnutapp.data.remote.models.revisioncorner.Stats
import com.doubtnutapp.databinding.ItemRevisionCornerStatsBinding
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.revisioncorner.ui.adapter.ScoreAdapter
import com.doubtnutapp.revisioncorner.ui.adapter.SubjectProgressAdapter

/**
 * Created by devansh on 17/08/21.
 */

class StatsViewHolder(itemView: View) : BaseViewHolder<Stats>(itemView) {

    private val binding = ItemRevisionCornerStatsBinding.bind(itemView)

    override fun bind(data: Stats) {
        with(binding) {
            tvTitle.text = data.title

            buttonViewAll.apply {
                isVisible = data.ctaText.isNotNullAndNotEmpty()
                text = data.ctaText
                setOnClickListener {
                    performAction(RcStatsViewAllClick(data.ctaDeeplink, data.title))
                }
            }

            rvScore.adapter = ScoreAdapter().apply {
                updateList(data.scores)
            }
            rvScore.disableTouchEvents()

            rvProgress.adapter = SubjectProgressAdapter().apply {
                updateList(data.subjectProgressItems)
            }
            rvProgress.disableTouchEvents()
        }
    }
}