package com.doubtnutapp.doubtfeed.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.data.remote.models.doubtfeed.Topic
import com.doubtnutapp.doubtfeed.ui.viewholder.TopicViewHolder

/**
 * Created by devansh on 1/5/21.
 */

class TopicAdapter(private val actionsPerformer: ActionPerformer?) :
    RecyclerView.Adapter<TopicViewHolder>() {

    private val topics = mutableListOf<Topic>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        return TopicViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_doubt_feed_topic, parent, false)
        ).apply {
            actionPerformer = actionsPerformer
        }
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        holder.bind(topics[position])
    }

    override fun getItemCount(): Int = topics.size

    fun updateList(topics: List<Topic>) {
        this.topics.clear()
        this.topics.addAll(topics)
        notifyDataSetChanged()
    }
}
