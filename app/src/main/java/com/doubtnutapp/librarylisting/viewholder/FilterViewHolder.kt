package com.doubtnutapp.librarylisting.viewholder

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.base.Filter
import com.doubtnutapp.databinding.ItemFilterLibraryBinding
import com.doubtnutapp.librarylisting.model.FilterInfo

/**
 * Created by Anand Gaurav on 2019-10-14.
 */
class FilterViewHolder(private val binding: ItemFilterLibraryBinding,
                       private val recyclerViewChild: RecyclerView) : BaseViewHolder<FilterInfo>(binding.root) {
    override fun bind(data: FilterInfo) {
        binding.executePendingBindings()
        binding.buttonFilter.text = data.title
        binding.buttonFilter.isSelected = data.isSelected
        binding.layoutParent.isSelected = data.isSelected
        binding.buttonFilter.setOnClickListener {
            if (recyclerViewChild.scrollState != SCROLL_STATE_IDLE || it.isSelected) {
                return@setOnClickListener
            } else {
                performAction(Filter(data.id, adapterPosition, data.title))
            }
        }
    }
}