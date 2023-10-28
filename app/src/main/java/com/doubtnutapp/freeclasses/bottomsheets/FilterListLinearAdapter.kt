package com.doubtnutapp.freeclasses.bottomsheets

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.databinding.ItemFilterListBinding

/**
 * Created by Akshat Jindal on 31/01/22.
 */
class FilterListLinearAdapter(
    val list: List<FilterListData.FilterListItem>,
    val onItemClicked: (pos: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FilterListSingleColumnViewHolder(
            ItemFilterListBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
    }


    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FilterListSingleColumnViewHolder).bind(list[position], position, onItemClicked)
    }

    class FilterListSingleColumnViewHolder(
        val binding: ItemFilterListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            data: FilterListData.FilterListItem,
            position: Int,
            onItemClicked: (pos: Int) -> Unit
        ) {
            binding.apply {
                text.text = data.title
                radioButton.isChecked = data.isSelected
                if (data.isSelected) {
                    text.setTextColor(Color.parseColor("#424242"))
                } else {
                    text.setTextColor(Color.parseColor("#808080"))
                }
                root.setOnClickListener {
                    onItemClicked(position)
                }
            }
        }
    }
}