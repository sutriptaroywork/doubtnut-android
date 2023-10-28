package com.doubtnutapp.freeclasses.bottomsheets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FilterButtonItemBinding

/**
 * Created by Akshat Jindal on 31/01/22.
 */
class FilterListGridAdapter(
    val list: List<FilterListData.FilterListItem>,
    val onItemClicked: (pos: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return FilterListGridViewHolder(
            FilterButtonItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FilterListGridViewHolder).bind(list[position], position, onItemClicked)
    }

    class FilterListGridViewHolder(
        val binding: FilterButtonItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            data: FilterListData.FilterListItem,
            position: Int,
            onItemClicked: (pos: Int) -> Unit
        ) {
            binding.apply {
                textView.text = data.title
                if (data.isSelected) {
                    textView.setTextColor(ContextCompat.getColor(root.context, R.color.white))
                    textView.background =
                        (ContextCompat.getDrawable(root.context, R.drawable.bg_orange_rectangle))
                } else {
                    textView.setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            R.color.color_969696
                        )
                    )
                    textView.background =
                        (ContextCompat.getDrawable(root.context, R.drawable.bg_grey_rectangle))
                }
                root.setOnClickListener {
                    onItemClicked(position)
                }
            }
        }
    }
}