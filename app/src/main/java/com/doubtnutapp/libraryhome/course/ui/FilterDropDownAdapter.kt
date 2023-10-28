package com.doubtnutapp.libraryhome.course.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.utils.Utils

/**
 * Created by Anand Gaurav on 19/04/20.
 */
class FilterDropDownAdapter(private val tripleList: List<Triple<String, String, String>>) : RecyclerView.Adapter<FilterDropDownAdapter.FilterViewHolder>() {
    private var selectedListener: FilterSelectedListener? = null

    fun setCategorySelectedListener(filterSelectedListener: FilterSelectedListener?) {
        this.selectedListener = filterSelectedListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        return FilterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_filter_drop_down, parent, false))
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val triple: Triple<String, String, String> = tripleList[position]
        if (triple.third.equals("mix", true)) {
            holder.ivIcon.visibility = View.VISIBLE
            holder.ivBackground.visibility = View.INVISIBLE
        } else {
            holder.ivIcon.visibility = View.INVISIBLE
            holder.ivBackground.visibility = View.VISIBLE
            holder.ivBackground.setBackgroundColor(Utils.parseColor(triple.third))
        }

        holder.tvDisplay.text = triple.second
        holder.itemView.setOnClickListener {
            selectedListener?.onFilterSelected(position, triple)
        }
    }

    override fun getItemCount(): Int {
        return tripleList.size
    }

    class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDisplay: AppCompatTextView = itemView.findViewById(R.id.tvDisplay)
        var ivIcon: AppCompatImageView = itemView.findViewById(R.id.ivIcon)
        var ivBackground: AppCompatImageView = itemView.findViewById(R.id.ivBackground)
    }

    interface FilterSelectedListener {
        fun onFilterSelected(position: Int, triple: Triple<String, String, String>?)
    }

}