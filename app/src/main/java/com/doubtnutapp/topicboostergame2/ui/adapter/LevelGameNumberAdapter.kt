package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.topicboostergame2.LevelGameNumber
import com.doubtnutapp.topicboostergame2.ui.viewholder.LevelGameNumberViewHolder

/**
 * Created by devansh on 14/06/21.
 */

class LevelGameNumberAdapter : RecyclerView.Adapter<LevelGameNumberViewHolder>() {

    private val levelQuestionNumbers = mutableListOf<LevelGameNumber>()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): LevelGameNumberViewHolder = LevelGameNumberViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic_booster_game_level_game_number, parent, false)
    )

    override fun onBindViewHolder(holder: LevelGameNumberViewHolder, position: Int) {
        holder.bind(levelQuestionNumbers[position])
    }

    override fun getItemCount(): Int = levelQuestionNumbers.size

    fun updateList(levelGameNumbers: List<LevelGameNumber>) {
        this.levelQuestionNumbers.clear()
        this.levelQuestionNumbers.addAll(levelGameNumbers)
        notifyDataSetChanged()
    }
}