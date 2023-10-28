package com.doubtnutapp.reward.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.data.remote.models.reward.Reward
import com.doubtnutapp.reward.ui.viewholder.RewardViewHolder

class RewardAdapter(private val actionPerformer: ActionPerformer?) :
    RecyclerView.Adapter<RewardViewHolder>() {

    private val rewards = mutableListOf<Reward>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        return RewardViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_reward, parent, false)
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