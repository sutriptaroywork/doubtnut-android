package com.doubtnutapp.revisioncorner.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.revisioncorner.Subject
import com.doubtnutapp.revisioncorner.ui.viewholder.ChooseSubjectViewHolder

class ChooseSubjectAdapter : RecyclerView.Adapter<ChooseSubjectViewHolder>() {

    private val subjects = mutableListOf<Subject>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseSubjectViewHolder =
        ChooseSubjectViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_bottom_sheet, parent, false))

    override fun onBindViewHolder(holder: ChooseSubjectViewHolder, position: Int) {
        holder.bind(subjects[position])
    }

    override fun getItemCount(): Int = subjects.size

    fun updateList(subjects: List<Subject>) {
        this.subjects.clear()
        this.subjects.addAll(subjects)
        notifyDataSetChanged()
    }
}