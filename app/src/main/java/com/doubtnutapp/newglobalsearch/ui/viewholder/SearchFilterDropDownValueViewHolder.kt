package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.content.Context
import android.graphics.Typeface
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.IasFilterValueDeselected
import com.doubtnutapp.base.IasFilterValueSelected
import com.doubtnutapp.databinding.ItemFilterValueIasBinding
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterValueAdapter

class SearchFilterDropDownValueViewHolder(
    val context: Context,
    val binding: ItemFilterValueIasBinding,
    private val isYoutube: Boolean,
    val actionPerformer: ActionPerformer,
    private val isFromAllChapters: Boolean
) : SearchFilterValueAdapter.IASFilterValueViewHolder(binding.root) {

    fun bind(
        filter: SearchFilterItem,
        position: Int,
        filterTypePosition: Int,
        isFromAllFilterScreen: Boolean = false
    ) {
        binding.filterValue = filter
        binding.multiTagItem.isSelected = filter.isSelected
        binding.multiTagItem.isEnabled = !filter.isDisabled()

        if (isFromAllFilterScreen) {
            binding.root.setPadding(0, 0, 20, 20)
        }
        if (filter.isSelected && !filter.isDisabled()) {
            binding.multiTagItem.setTypeface(null, Typeface.BOLD)
        } else {
            binding.multiTagItem.setTypeface(null, Typeface.NORMAL)
        }

        binding.root.setOnClickListener {
            if (!filter.isDisabled()) {
                if (filter.isSelected && !isFromAllChapters) {
                    actionPerformer.performAction(
                        IasFilterValueDeselected(
                            filter,
                            position,
                            filterTypePosition,
                            isYoutube
                        )
                    )
                    filter.isSelected = false
                } else {
                    actionPerformer.performAction(
                        IasFilterValueSelected(
                            filter,
                            position,
                            filterTypePosition,
                            isYoutube
                        )
                    )
                    filter.isSelected = true
                }
                binding.multiTagItem.isSelected = filter.isSelected
            }
        }
    }

}