package com.doubtnutapp.doubtfeed2.reward.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.doubtfeed2.reward.data.model.Reward
import com.doubtnutapp.doubtfeed2.reward.ui.viewholder.RewardViewHolder

/**
 * Created by devansh on 14/7/21.
 */

class RewardAdapter(private val actionPerformer: ActionPerformer?) :
    RecyclerView.Adapter<RewardViewHolder>() {

    private val rewards = mutableListOf<Reward>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        return RewardViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_doubt_feed_reward, parent, false)
        ).apply { actionPerformer = this@RewardAdapter.actionPerformer }
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        holder.bind(rewards[position])
    }

    override fun getItemCount(): Int = rewards.size

    fun updateList(rewards: List<Reward>) {
        this.rewards.clear()
        this.rewards.addAll(rewards)
        notifyDataSetChanged()
    }
}
