package com.doubtnutapp.scheduledquiz.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ItemDownloadPaperBinding
import com.doubtnutapp.loadImage
import com.doubtnutapp.scheduledquiz.di.model.QuizSubject

class QuizSubjectAdapter(private val listings: List<QuizSubject>?) :
    RecyclerView.Adapter<QuizSubjectAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_download_paper,
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.ivSubject.loadImage(listings!![position].iconUrl.orEmpty())
        holder.binding.tvSubject.text = listings[position].name
    }

    override fun getItemCount(): Int {
        return listings!!.size
    }

    inner class ViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding = ItemDownloadPaperBinding.bind(itemView)

    }

}