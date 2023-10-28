package com.doubtnutapp.libraryhome.coursev3.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.CourseFilterTypeData
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.setVisibleState

class CourseFilterDropDownAdapter(private val list: List<CourseFilterTypeData>) : RecyclerView.Adapter<CourseFilterDropDownAdapter.FilterViewHolder>() {
    private var selectedListener: FilterSelectedListener? = null

    fun setCategorySelectedListener(filterSelectedListener: FilterSelectedListener?) {
        this.selectedListener = filterSelectedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        return FilterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_course_filter_drop_down, parent, false))
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val data: CourseFilterTypeData = list[position]
        holder.tvDisplay.text = data.display
        if (data.isSelected == true) {
            holder.tvDisplay.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_small_tick, 0)
        } else {
            holder.tvDisplay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        if (!data.iconUrl.isNullOrEmpty()) {
            holder.iconIv.setVisibleState(false)
        } else {
            holder.iconIv.setVisibleState(true)
            holder.iconIv.loadImageEtx(data.iconUrl.orEmpty())
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
        var iconIv: ImageView = itemView.findViewById(R.id.iconIv)
    }

    interface FilterSelectedListener {
        fun onFilterSelected(position: Int, data: CourseFilterTypeData)
    }
}