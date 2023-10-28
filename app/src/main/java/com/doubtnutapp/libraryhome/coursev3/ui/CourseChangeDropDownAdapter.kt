package com.doubtnutapp.libraryhome.coursev3.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.CourseSelectionData

class CourseChangeDropDownAdapter(private val list: List<CourseSelectionData.DropDownData>) : RecyclerView.Adapter<CourseChangeDropDownAdapter.FilterViewHolder>() {
    private var selectedListener: FilterSelectedListener? = null

    fun setCategorySelectedListener(filterSelectedListener: FilterSelectedListener?) {
        this.selectedListener = filterSelectedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        return FilterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_course_filter_drop_down, parent, false))
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val data: CourseSelectionData.DropDownData = list[position]
        holder.tvDisplay.text = data.text
        if (data.isSelected == true) {
            holder.tvDisplay.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_small_tick, 0)
        } else {
            holder.tvDisplay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        holder.itemView.setOnClickListener {
            selectedListener?.onFilterSelected(position, data)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDisplay: AppCompatTextView = itemView.findViewById(R.id.tvDisplay)
    }

    interface FilterSelectedListener {
        fun onFilterSelected(position: Int, data: CourseSelectionData.DropDownData)
    }
}