package com.doubtnutapp.revisioncorner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.data.remote.models.revisioncorner.SubjectProgress
import com.doubtnutapp.databinding.ItemRevisionCornerSubjectProgressBinding
import com.doubtnutapp.revisioncorner.ui.viewholder.SubjectProgressViewHolder

/**
 * Created by devansh on 17/08/21.
 */

class SubjectProgressAdapter : RecyclerView.Adapter<SubjectProgressViewHolder>() {

    private val subjectProgressItems = mutableListOf<SubjectProgress>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectProgressViewHolder {
        return SubjectProgressViewHolder(
            ItemRevisionCornerSubjectProgressBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root
        )
    }

    override fun onBindViewHolder(holder: SubjectProgressViewHolder, position: Int) {
        holder.bind(subjectProgressItems[position])
    }

    override fun getItemCount(): Int = subjectProgressItems.size

    fun updateList(subjectProgressItems: List<SubjectProgress>) {
        this.subjectProgressItems.clear()
        this.subjectProgressItems.addAll(subjectProgressItems)
        notifyDataSetChanged()
    }
}