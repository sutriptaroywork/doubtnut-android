package com.doubtnutapp.gamification.badgesscreen.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.gamification.badgesscreen.model.BaseBadgeViewType
import com.doubtnutapp.gamification.badgesscreen.ui.viewholder.BadgeViewHolderFactory


class BadgesViewAdapter(private val actionsPerformer: ActionPerformer?) : RecyclerView.Adapter<BaseViewHolder<BaseBadgeViewType>>() {

    private val recyclerViewPool = RecyclerView.RecycledViewPool()
    private val viewHolderFactory: BadgeViewHolderFactory = BadgeViewHolderFactory(recyclerViewPool)
    private val badgeList = mutableListOf<BaseBadgeViewType>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<BaseBadgeViewType> {
        return (viewHolderFactory.getViewHolderFor(parent, viewType) as BaseViewHolder<BaseBadgeViewType>).apply {
            actionPerformer = this@BadgesViewAdapter.actionsPerformer
        }
    }

    override fun getItemCount(): Int = badgeList.size

    override fun getItemViewType(position: Int): Int {
        return badgeList[position].viewType
    }

    override fun onBindViewHolder(holder: BaseViewHolder<BaseBadgeViewType>, position: Int) {
        holder.bind(badgeList[position])
    }

    fun updateBadges(recentbadgeList: List<BaseBadgeViewType>) {
        val startingPosition = badgeList.size
        badgeList.addAll(recentbadgeList)
        notifyItemRangeInserted(startingPosition, recentbadgeList.size)
    }


}