package com.doubtnutapp.reward.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.reward.ui.viewholder.RewardTermsViewHolder

class RewardTermsAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<RewardTermsViewHolder>() {

    private val termsList = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardTermsViewHolder {
        return RewardTermsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_reward_terms, parent, false)
        ).apply { actionPerformer = actionsPerformer }
    }

    override fun onBindViewHolder(holder: RewardTermsViewHolder, position: Int) {
        holder.bind(termsList[position])
    }

    override fun getItemCount(): Int = termsList.size

    fun updateList(knowMoreItems: List<String>) {
        this.termsList.clear()
        this.termsList.addAll(knowMoreItems)
        notifyDataSetChanged()
    }

}