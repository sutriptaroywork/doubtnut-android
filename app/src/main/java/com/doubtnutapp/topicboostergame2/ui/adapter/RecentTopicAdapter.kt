package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.topicboostergame2.Topic
import com.doubtnutapp.topicboostergame2.ui.viewholder.RecentTopicViewHolder

/**
 * Created by devansh on 15/06/21.
 */

class RecentTopicAdapter : RecyclerView.Adapter<RecentTopicViewHolder>() {

    private val topics = mutableListOf<Topic>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentTopicViewHolder =
        RecentTopicViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topic_booster_game_recent_topic, parent, false)
        )

    override fun onBindViewHolder(holder: RecentTopicViewHolder, position: Int) {
        holder.bind(topics[position])
    }

    override fun getItemCount(): Int = topics.size

    fun updateList(topics: List<Topic>) {
        this.topics.clear()
        this.topics.addAll(topics)
        notifyDataSetChanged()
    }
}