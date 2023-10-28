package com.doubtnutapp.gamification.earnedPointsHistory.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.gamification.earnedPointsHistory.ui.viewholder.EarnedPointsHistoryHeaderViewHolder
import com.doubtnutapp.gamification.earnedPointsHistory.ui.viewholder.EarnedPointsHistoryViewHolder

/**
 * Created by akshaynandwana on
 * 05, March, 2019
 **/
class EarnedPointsFeedViewHolderFactory {

    fun getViewHolderFor(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            R.layout.item_earned_point_history -> EarnedPointsHistoryViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_earned_point_history, parent, false)
            )
            R.layout.item_earned_point_history_header -> EarnedPointsHistoryHeaderViewHolder(
                    DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_earned_point_history_header, parent, false)
            )

            else -> throw IllegalArgumentException()
        }
    }
}