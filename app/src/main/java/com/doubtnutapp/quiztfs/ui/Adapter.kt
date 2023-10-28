package com.doubtnutapp.quiztfs.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.quiztfs.LiveQuestionsSubject
import com.doubtnutapp.databinding.ItemSubjectSelectBinding
import com.doubtnutapp.loadImage

/**
 * Created by Mehul Bisht on 01-09-2021
 */
class Adapter(
    private val onItemSelected: (LiveQuestionsSubject) -> Unit
) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    private var subjects: List<LiveQuestionsSubject> = listOf()

    inner class ViewHolder(private val binding: ItemSubjectSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val background = binding.background
        val checkbox = binding.checkbox
        val icon = binding.icon
        val title = binding.title

        fun bind(data: LiveQuestionsSubject) {

            title.text = data.title
            icon.loadImage(data.imageUrl)

            if (data.isSelected) {
                checkbox.visibility = View.VISIBLE
                background.background =
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.background_selected_subject
                    )
            } else {
                checkbox.visibility = View.GONE
                background.background =
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.background_select_subject
                    )
                title.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.grey_504949
                    )
                )
            }
        }
    }

    fun setData(subjects: List<LiveQuestionsSubject>) {
        this.subjects = subjects
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemSubjectSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = subjects[position]
        holder.bind(data)
        holder.itemView.setOnClickListener {
            onItemSelected(data)
        }
    }

    override fun getItemCount(): Int {
        return subjects.size
    }
}