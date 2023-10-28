package com.doubtnutapp.newglobalsearch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.IasFilterValueDeselected
import com.doubtnutapp.base.IasFilterValueSelected
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.viewholder.SearchFilterDropDownValueViewHolder

class SearchFilterValueDropDownAdapter(
    private val actionPerformer: ActionPerformer?,
    var facet: SearchTabsItem,
    private val isYoutube: Boolean,
    val isFromAllChapters: Boolean
) :
    RecyclerView.Adapter<SearchFilterDropDownValueViewHolder>(),
    ActionPerformer {

    private var filterValues: ArrayList<SearchFilterItem> = arrayListOf()
    private var filterTypePosition = 0
    private var selectedFilterValue = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchFilterDropDownValueViewHolder {
        return SearchFilterDropDownValueViewHolder(
            context = parent.context,
            binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_filter_value_ias,
                parent,
                false
            ),
            isYoutube = isYoutube,
            actionPerformer = this,
            isFromAllChapters = isFromAllChapters
        )
    }

    override fun getItemCount(): Int {
        return filterValues.size
    }

    override fun onBindViewHolder(holder: SearchFilterDropDownValueViewHolder, position: Int) {
        if (holder is SearchFilterDropDownValueViewHolder && position < filterValues.size) {
            holder.bind(
                filterValues[position],
                position,
                filterTypePosition,
                false
            )
        }
    }

    fun updateData(
        filter: ArrayList<SearchFilterItem>,
        filterTypePosition: Int,
        facet: SearchTabsItem
    ) {
        this.facet = facet
        this.filterTypePosition = filterTypePosition

        if (filter.isNullOrEmpty()) {
            filterValues.clear()
        } else {
            var index = 0
            for (f in filter) {
                if (f.isSelected) {
                    selectedFilterValue = index
                    break
                }
                index++
            }
            filterValues.clear()
            filterValues.addAll(filter)

        }
        notifyDataSetChanged()
    }

    override fun performAction(action: Any) {
        if (action is IasFilterValueSelected) {
            if (selectedFilterValue >= 0 && selectedFilterValue < filterValues.size) {
                filterValues[selectedFilterValue].isSelected = false
                notifyItemChanged(selectedFilterValue)
            }
            selectedFilterValue = action.position
            filterValues[selectedFilterValue].isSelected = true

            notifyItemChanged(selectedFilterValue)
        }
        if (action is IasFilterValueDeselected) {
            selectedFilterValue = action.position
            filterValues[selectedFilterValue].isSelected = false

            selectedFilterValue = action.position
            filterValues[selectedFilterValue].isSelected = false

            notifyItemChanged(selectedFilterValue)
        }

        actionPerformer?.performAction(action)
    }
}