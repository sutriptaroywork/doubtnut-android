package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.IasFilterTypeDeselected
import com.doubtnutapp.base.IasFilterTypeSelected
import com.doubtnutapp.databinding.ItemIasFilterTypeBinding
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.hide
import com.doubtnutapp.show

class SearchFilterTypeViewHolder(
    val context: Context,
    val view: View,
    val actionPerformer: ActionPerformer
) : RecyclerView.ViewHolder(view) {

    private val KEY_SORT: String = "sort"

    val binding = ItemIasFilterTypeBinding.bind(itemView)

    fun bind(filter: SearchFilter, position: Int, isCurrent: Boolean) {
        binding.filterTypeLabel.text = filter.label
        binding.root.isSelected = filter.isSelected
        if (filter.isSelected) {
            binding.selectedDot.show()
        } else {
            binding.selectedDot.hide()
        }

        if (isCurrent) {
            binding.root.setBackgroundResource(R.drawable.bg_ias_filter_tags_selected)
        } else {
            binding.root.setBackgroundResource(R.drawable.bg_ias_filter_tags)
        }

        if (filter.isExpanded) {
            binding.imgDropDown.setImageResource(R.drawable.ic_up_arrow)
        } else {
            binding.imgDropDown.setImageResource(R.drawable.ic_drop_down)
        }

        binding.root.setOnClickListener {
            filter.isExpanded = !filter.isExpanded
            if (filter.isExpanded) {
                binding.imgDropDown.setImageResource(R.drawable.ic_up_arrow)
                actionPerformer.performAction(IasFilterTypeSelected(binding.root, filter, position))
            } else {
                binding.imgDropDown.setImageResource(R.drawable.ic_drop_down)
                actionPerformer.performAction(IasFilterTypeDeselected(filter, position))
            }
        }
    }
}