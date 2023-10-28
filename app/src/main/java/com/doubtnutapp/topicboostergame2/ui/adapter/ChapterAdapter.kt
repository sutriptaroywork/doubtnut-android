package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.data.remote.models.topicboostergame2.Topic
import com.doubtnutapp.topicboostergame2.ui.viewholder.ChapterViewHolder
import okhttp3.internal.notifyAll

class ChapterAdapter(val navResultConstant: String, private val actionsPerformer: ActionPerformer2?) : RecyclerView.Adapter<ChapterViewHolder>() {

    private var chapters = mutableListOf<Topic>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder =
        ChapterViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chapter_bottom_sheet, parent, false), navResultConstant).apply {
                    actionPerformer = actionsPerformer
        }


    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(Pair(chapters[position], position))
    }

    override fun getItemCount(): Int = chapters.size

    fun updateList(topics: List<Topic>) {
        this.chapters.clear()
        this.chapters.addAll(topics)
        notifyDataSetChanged()
    }
}