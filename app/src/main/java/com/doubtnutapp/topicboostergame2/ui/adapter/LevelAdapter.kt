package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.topicboostergame2.Level
import com.doubtnutapp.topicboostergame2.ui.viewholder.LevelViewHolder

/**
 * Created by devansh on 17/06/21.
 */

class LevelAdapter : RecyclerView.Adapter<LevelViewHolder>() {

    private val levels = mutableListOf<Level>()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): LevelViewHolder = LevelViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_level_bottom_sheet, parent, false)
    )

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        holder.bind(levels[position])
    }

    override fun getItemCount(): Int = levels.size

    fun updateList(levels: List<Level>) {
        this.levels.clear()
        this.levels.addAll(levels)
        notifyDataSetChanged()
    }
}