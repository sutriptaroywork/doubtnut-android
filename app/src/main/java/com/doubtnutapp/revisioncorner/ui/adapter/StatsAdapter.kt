package com.doubtnutapp.revisioncorner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.data.remote.models.revisioncorner.Stats
import com.doubtnutapp.databinding.ItemRevisionCornerStatsBinding
import com.doubtnutapp.revisioncorner.ui.viewholder.StatsViewHolder

/**
 * Created by devansh on 17/08/21.
 */

class StatsAdapter(private val actionPerformer: ActionPerformer2) :
    RecyclerView.Adapter<StatsViewHolder>() {

    private val statsItems = mutableListOf<Stats>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        return StatsViewHolder(
            ItemRevisionCornerStatsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root
        ).apply {
            actionPerformer = this@StatsAdapter.actionPerformer
        }
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        holder.bind(statsItems[position])
    }

    override fun getItemCount(): Int = statsItems.size

    fun updateList(statsItems: List<Stats>) {
        this.statsItems.clear()
        this.statsItems.addAll(statsItems)
        notifyDataSetChanged()
    }
}