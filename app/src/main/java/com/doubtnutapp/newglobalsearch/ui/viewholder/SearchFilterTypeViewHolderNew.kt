package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.IasFilterTypeSelected
import com.doubtnutapp.databinding.ItemFilterTypeIasBinding
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.hide
import com.doubtnutapp.show

class SearchFilterTypeViewHolderNew(
    val context: Context,
    val view: View,
    val actionPerformer: ActionPerformer
) : RecyclerView.ViewHolder(view) {

    val binding = ItemFilterTypeIasBinding.bind(itemView)

    fun bind(filter: SearchFilter, position: Int, isCurrent: Boolean) {

        if (filter.appliedLabel.isNullOrEmpty()) {
            binding.filterTypeLabel.text = filter.label
        } else {
            binding.filterTypeLabel.text = filter.appliedLabel
        }
        view.isSelected = filter.isSelected
        if (filter.isSelected) {
            binding.selectedDot.show()
        } else {
            binding.selectedDot.hide()
        }

        if (isCurrent) {
            view.setBackgroundResource(R.drawable.bg_ias_filter_tags_selected_gray)
        } else {
            view.setBackgroundResource(R.drawable.bg_ias_filter_tags_gray)
        }

        view.setOnClickListener {
            actionPerformer.performAction(IasFilterTypeSelected(view, filter, position))
        }
    }
}