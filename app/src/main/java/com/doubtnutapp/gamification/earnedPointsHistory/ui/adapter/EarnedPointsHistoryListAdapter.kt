package com.doubtnutapp.gamification.earnedPointsHistory.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.gamification.earnedPointsHistory.model.EarnedPointsBaseFeedViewItem
import com.doubtnutapp.gamification.earnedPointsHistory.ui.EarnedPointsFeedViewHolderFactory

class EarnedPointsHistoryListAdapter(private val actionsPerformer: ActionPerformer?) : RecyclerView.Adapter<BaseViewHolder<EarnedPointsBaseFeedViewItem>>() {

    private val viewHolderFactory: EarnedPointsFeedViewHolderFactory = EarnedPointsFeedViewHolderFactory()
    private val feeds = mutableListOf<EarnedPointsBaseFeedViewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<EarnedPointsBaseFeedViewItem> {
        return (viewHolderFactory.getViewHolderFor(parent, viewType) as BaseViewHolder<EarnedPointsBaseFeedViewItem>).apply {
            actionPerformer = this@EarnedPointsHistoryListAdapter.actionsPerformer
        }
    }

    override fun getItemCount(): Int = feeds.size

    override fun getItemViewType(position: Int): Int {
        return feeds[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<EarnedPointsBaseFeedViewItem>, position: Int) {
        holder.bind(feeds[position])
    }

    fun updateFeeds(recentFeeds: List<EarnedPointsBaseFeedViewItem>) {
        val startingPosition = feeds.size
        feeds.addAll(recentFeeds)
        notifyItemRangeInserted(startingPosition, recentFeeds.size)
    }
}