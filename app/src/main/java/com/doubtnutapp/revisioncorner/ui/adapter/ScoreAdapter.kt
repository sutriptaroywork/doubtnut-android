package com.doubtnutapp.revisioncorner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.data.remote.models.revisioncorner.Score
import com.doubtnutapp.databinding.ItemRevisionCornerScoreBinding
import com.doubtnutapp.revisioncorner.ui.viewholder.ScoreViewHolder

/**
 * Created by devansh on 17/08/21.
 */

class ScoreAdapter : RecyclerView.Adapter<ScoreViewHolder>() {

    private val scoreItems = mutableListOf<Score>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        return ScoreViewHolder(
            ItemRevisionCornerScoreBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root
        )
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        holder.bind(scoreItems[position])
    }

    override fun getItemCount(): Int = scoreItems.size

    fun updateList(scoreItems: List<Score>) {
        this.scoreItems.clear()
        this.scoreItems.addAll(scoreItems)
        notifyDataSetChanged()
    }
}