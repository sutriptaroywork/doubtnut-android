package com.doubtnutapp.reward.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.data.remote.models.reward.KnowMoreListItem
import com.doubtnutapp.reward.ui.viewholder.KnowMoreRewardViewHolder

class KnowMoreAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<KnowMoreRewardViewHolder>() {

    private val knowMoreItems = mutableListOf<KnowMoreListItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KnowMoreRewardViewHolder {
        return KnowMoreRewardViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reward_know_more, parent, false)
        ).apply { actionPerformer = actionsPerformer }
    }

    override fun onBindViewHolder(holder: KnowMoreRewardViewHolder, position: Int) {
        holder.bind(knowMoreItems[position])
    }

    override fun getItemCount(): Int = knowMoreItems.size

    fun updateList(knowMoreItems: List<KnowMoreListItem>) {
        this.knowMoreItems.clear()
        this.knowMoreItems.addAll(knowMoreItems)
        notifyDataSetChanged()
    }

}