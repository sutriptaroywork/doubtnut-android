package com.doubtnutapp.newlibrary.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearPapersFilter

/**
 * Created by Mehul Bisht on 26-11-2021
 */

class DropDownAdapter(private val list: List<LibraryPreviousYearPapersFilter>) :
    RecyclerView.Adapter<DropDownAdapter.FilterViewHolder>() {
    private var filterListener: FilterListener? = null

    fun setFilterListener(filterListener: FilterListener) {
        this.filterListener = filterListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        return FilterViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_drop_down, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val data: LibraryPreviousYearPapersFilter = list[position]
        holder.tvDisplay.text = data.filterText

        holder.tvDisplay.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

        holder.itemView.setOnClickListener {
            filterListener?.onSelected(position, data)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDisplay: AppCompatTextView = itemView.findViewById(R.id.tvDisplay)
    }

    interface FilterListener {
        fun onSelected(position: Int, data: LibraryPreviousYearPapersFilter)
    }
}