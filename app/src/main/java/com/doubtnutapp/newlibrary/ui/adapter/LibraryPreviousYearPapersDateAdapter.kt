package com.doubtnutapp.newlibrary.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.databinding.ItemLibrarySortByYearBinding
import com.doubtnutapp.newlibrary.model.SelectedFilterItem

/**
 * Created by Mehul Bisht on 26/11/21
 */

class LibraryPreviousYearPapersDateAdapter(
    private val onClick: (String?) -> Unit
) :
    RecyclerView.Adapter<LibraryPreviousYearPapersDateAdapter.ViewHolder>() {

    private var byYearItems = mutableListOf<SelectedFilterItem>()

    inner class ViewHolder(private val binding: ItemLibrarySortByYearBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SelectedFilterItem) {
            binding.apply {
                if (item.isSelected) {
                    date.setTextColor(Color.WHITE)
                    dateParent.setBackgroundColor(Color.parseColor("#ea532c"))
                } else {
                    date.setTextColor(Color.BLACK)
                    dateParent.setBackgroundColor(Color.WHITE)
                }
                date.text = item.text
                dateParent.setOnClickListener {
                    onClick(item.id)
                }
            }
        }
    }

    fun submitData(items: List<SelectedFilterItem>) {
        byYearItems.clear()
        byYearItems.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLibrarySortByYearBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = byYearItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return byYearItems.size
    }
}