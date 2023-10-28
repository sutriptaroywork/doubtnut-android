package com.doubtnutapp.topicboostergame2.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.data.remote.models.topicboostergame2.Subject
import com.doubtnutapp.topicboostergame2.ui.viewholder.SubjectViewHolder

/**
 * Created by devansh on 15/06/21.
 */

class SubjectAdapter(private val actionsPerformer: ActionPerformer2) : RecyclerView.Adapter<SubjectViewHolder>() {

    private val subjects = mutableListOf<Subject>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder =
        SubjectViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_topic_booster_game_subject, parent, false)
        ).apply {
            actionPerformer = actionsPerformer
        }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(subjects[position])
    }

    override fun getItemCount(): Int = subjects.size

    fun updateList(subjects: List<Subject>) {
        this.subjects.clear()
        this.subjects.addAll(subjects)
        notifyDataSetChanged()
    }
}