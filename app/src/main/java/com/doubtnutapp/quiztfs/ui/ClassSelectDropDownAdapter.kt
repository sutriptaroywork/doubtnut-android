package com.doubtnutapp.quiztfs.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.quiztfs.LiveQuestionsClass

/**
 * Created by Mehul Bisht on 26-08-2021
 */
class ClassSelectDropDownAdapter(private val list: List<LiveQuestionsClass>) :
    RecyclerView.Adapter<ClassSelectDropDownAdapter.ClassSelectViewHolder>() {
    private var classSelectedListener: ClassSelectedListener? = null

    fun setClassSelectedListener(classSelectedListener: ClassSelectedListener) {
        this.classSelectedListener = classSelectedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassSelectViewHolder {
        return ClassSelectViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_course_filter_drop_down, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ClassSelectViewHolder, position: Int) {
        val data: LiveQuestionsClass = list[position]
        holder.tvDisplay.text = data.title
        if (data.isSelected) {
            holder.tvDisplay.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_small_tick,
                0
            )
        } else {
            holder.tvDisplay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        holder.itemView.setOnClickListener {
            classSelectedListener?.onClassSelected(position, data)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ClassSelectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDisplay: AppCompatTextView = itemView.findViewById(R.id.tvDisplay)
    }

    interface ClassSelectedListener {
        fun onClassSelected(position: Int, data: LiveQuestionsClass)
    }
}