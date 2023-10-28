package com.doubtnutapp.doubtfeed2.reward.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.doubtfeed2.reward.data.model.KnowMoreItem
import com.doubtnutapp.doubtfeed2.reward.ui.viewholder.KnowMoreRewardViewHolder

/**
 * Created by devansh on 14/7/21.
 */

class KnowMoreAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<KnowMoreRewardViewHolder>() {

    private val knowMoreItems = mutableListOf<KnowMoreItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KnowMoreRewardViewHolder {
        return KnowMoreRewardViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_doubt_feed_reward_know_more, parent, false)
        ).apply { actionPerformer = actionsPerformer }
    }

    override fun onBindViewHolder(holder: KnowMoreRewardViewHolder, position: Int) {
        holder.bind(knowMoreItems[position])
    }

    override fun getItemCount(): Int = knowMoreItems.size

    fun updateList(knowMoreItems: List<KnowMoreItem>) {
        this.knowMoreItems.clear()
        this.knowMoreItems.addAll(knowMoreItems)
        notifyDataSetChanged()
    }
}
