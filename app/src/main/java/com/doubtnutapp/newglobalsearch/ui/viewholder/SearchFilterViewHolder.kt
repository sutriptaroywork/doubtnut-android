package com.doubtnutapp.newglobalsearch.ui.viewholder

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.WidgetIasFilterBinding
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.hide
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterTypeAdapter
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterValueAdapter
import com.doubtnutapp.show

class SearchFilterViewHolder(
    val context: Context,
    val view: View,
    val actionPerformer: ActionPerformer,
    val isYoutube: Boolean,
    val isFromAllChapters: Boolean,
    val appliedFilter: HashMap<String, String>
) : RecyclerView.ViewHolder(view), ActionPerformer {

    private val KEY_SORT: String = "sort"

    private lateinit var valueAdapter: SearchFilterValueAdapter
    private lateinit var typeAdapter: SearchFilterTypeAdapter

    private var tab: SearchTabsItem? = null
    private var filterArr: ArrayList<SearchFilter> = arrayListOf()
    private var selectedFilterTypePosition = 0

    val binding = WidgetIasFilterBinding.bind(itemView)

    fun bind(
        filter: ArrayList<SearchFilter>,
        facet: SearchTabsItem,
        selectedFilterTypePosition: Int,
        position: Int
    ) {
        tab = facet
        filterArr.clear()
        filterArr.addAll(filter)
        typeAdapter = SearchFilterTypeAdapter(this)
        binding.rvFilterType.itemAnimator = null
        binding.rvFilterType.adapter = typeAdapter
        binding.rvFilterType.isNestedScrollingEnabled = true
        typeAdapter.updateData(filter, selectedFilterTypePosition)

        valueAdapter =
            SearchFilterValueAdapter(this, facet, isYoutube, isFromAllChapters, !isFromAllChapters)
        binding.rvFilterValues.itemAnimator = null
        val m = StaggeredGridLayoutManager(2, RecyclerView.HORIZONTAL)
        m.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        binding.rvFilterValues.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.rvFilterValues.adapter = valueAdapter
        if (selectedFilterTypePosition >= 0 && selectedFilterTypePosition < filter.size) {
            valueAdapter.updateData(
                filter[selectedFilterTypePosition].filters,
                selectedFilterTypePosition,
                tab!!
            )
        }
        this.selectedFilterTypePosition = selectedFilterTypePosition

    }

    override fun performAction(action: Any) {
        when (action) {
            is IasFilterTypeSelected -> {
                onFilterTypeSelected(action)
                actionPerformer.performAction(action)
            }
            is IasFilterTypeDeselected -> {
                binding.rvFilterValues.hide()
            }
            is IasFilterValueDeselected -> {
                actionPerformer.performAction(action)
                onFilterValueDeselected(action)
            }

            is IasFilterValueSelected -> {
                actionPerformer.performAction(action)
                onFilterValueSelected(action)
            }
            else -> actionPerformer.performAction(action)
        }
    }

    private fun onFilterTypeSelected(action: IasFilterTypeSelected) {
        binding.rvFilterValues.show()
        if (selectedFilterTypePosition < filterArr.size && selectedFilterTypePosition >= 0) {
            filterArr[selectedFilterTypePosition].isExpanded = false
        }
        selectedFilterTypePosition = action.position
        if (selectedFilterTypePosition < filterArr.size && selectedFilterTypePosition >= 0) {
            filterArr[selectedFilterTypePosition].isExpanded = true
        }
        if ("chapter".equals(
                filterArr[action.position].key,
                true
            ) && filterArr[action.position].filters.size > 4
        ) {
            valueAdapter.updateData(
                filterArr[action.position].filters.subList(0, 4)
                    .toList() as ArrayList<SearchFilterItem>, position, tab!!
            )
        } else {
            valueAdapter.updateData(filterArr[action.position].filters, position, tab!!)
        }
    }

    private fun onFilterValueSelected(action: IasFilterValueSelected) {
        if (isFromAllChapters) {
            appliedFilter.clear()
        }
        if (selectedFilterTypePosition >= 0 && selectedFilterTypePosition < filterArr.size) {
            filterArr[selectedFilterTypePosition].isSelected = true
        }

        if (KEY_SORT.equals(filterArr[selectedFilterTypePosition].key, true)) {
            actionPerformer.performAction(IasSortByFilterSelected(tab!!, action.filterValue))
        } else {
            if (filterArr[selectedFilterTypePosition].key.equals("course", true)) {
                actionPerformer.performAction(CourseV2FilterApplied())
                appliedFilter.clear()
            }
            appliedFilter.put(filterArr[selectedFilterTypePosition].key, action.filterValue.value)
            actionPerformer.performAction(IasFilterSelected(tab!!, appliedFilter, isYoutube))
        }
        valueAdapter.notifyDataSetChanged()
        binding.rvFilterValues.hide()
        filterArr[selectedFilterTypePosition].isExpanded = false
        typeAdapter.notifyItemChanged(selectedFilterTypePosition)
    }

    private fun onFilterValueDeselected(action: IasFilterValueDeselected) {

        filterArr[selectedFilterTypePosition].isSelected = false

        filterArr[selectedFilterTypePosition].isSelected = false
        if (!KEY_SORT.equals(filterArr[selectedFilterTypePosition].key, true)) {
            appliedFilter.remove(filterArr[selectedFilterTypePosition].key)
            if (isFromAllChapters) {
                appliedFilter.clear()
            }
            actionPerformer.performAction(IasFilterSelected(tab!!, appliedFilter, isYoutube))
        }
        filterArr[selectedFilterTypePosition].isExpanded = false
        typeAdapter.notifyItemChanged(selectedFilterTypePosition)
        binding.rvFilterValues.hide()
    }
}