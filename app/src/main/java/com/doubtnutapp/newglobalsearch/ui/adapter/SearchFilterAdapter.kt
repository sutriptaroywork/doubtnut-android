package com.doubtnutapp.newglobalsearch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.base.IasClearAllFilters
import com.doubtnutapp.base.IasFilterSelected
import com.doubtnutapp.base.IasFilterTypeSelected
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.viewholder.SearchFilterViewHolder

class SearchFilterAdapter(
    private val actionPerformer: ActionPerformer,
    val isYoutube: Boolean = false,
    val isFromAllChapters: Boolean = false
) :
    RecyclerView.Adapter<SearchFilterViewHolder>(), ActionPerformer {

    val appliedFilter: HashMap<String, String> = hashMapOf()
    private val searchFilterList: ArrayList<SearchFilter> = arrayListOf()
    private lateinit var facet: SearchTabsItem
    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchFilterViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.widget_ias_filter, null, false)
        view.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
        return SearchFilterViewHolder(
            parent.context,
            view,
            this,
            isYoutube,
            isFromAllChapters,
            appliedFilter
        )
    }

    override fun getItemCount(): Int = if (searchFilterList.isNullOrEmpty()) 0 else 1

    override fun onBindViewHolder(holder: SearchFilterViewHolder, position: Int) {
        holder.bind(searchFilterList, facet, selectedPosition, position)
    }

    fun updateData(tab: SearchTabsItem, data: List<SearchFilter>) {
        facet = tab
        searchFilterList.clear()
        if (selectedPosition >= data.size) {
            selectedPosition = 0
        }
        if (!data.isNullOrEmpty()) {
            searchFilterList.addAll(data)
            for (filter in searchFilterList) {
                for (item in filter.filters) {
                    if (item.isSelected) {
                        filter.isSelected = true
                        if (!isFromAllChapters) {
                            appliedFilter.put(filter.key, item.value)
                        }
                        break
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    override fun performAction(action: Any) {
        if (action is IasClearAllFilters) {
            for (filter in searchFilterList) {
                filter.isSelected = false
                if (!filter.filters.isNullOrEmpty()) {
                    for (filterValue in filter.filters) {
                        filterValue.isSelected = false
                    }
                }
            }
            appliedFilter.clear()
            actionPerformer.performAction(IasFilterSelected(facet!!, hashMapOf(), isYoutube))
            selectedPosition = 0
            notifyDataSetChanged()
        } else {
            if (action is IasFilterTypeSelected) {
                selectedPosition = action.position
            }
            actionPerformer.performAction(action)
        }
    }

    fun onAppliedFilterValueRemoved(toRemovefilterValue: String) {
        for (filter in searchFilterList) {
            if (filter.isSelected && !filter.filters.isNullOrEmpty()) {
                for (filterValue in filter.filters) {
                    if (filterValue.value.equals(toRemovefilterValue)) {
                        filterValue.isSelected = false
                        filter.isSelected = false
                        if (!"sort".equals(filter.key, true)) {
                            appliedFilter.remove(filter.key)
                            actionPerformer.performAction(
                                IasFilterSelected(
                                    facet!!,
                                    appliedFilter,
                                    isYoutube
                                )
                            )
                        }
                        notifyDataSetChanged()
                        break
                    }
                }
            }
        }
    }

    fun getSelectedFilterValues(): Map<out String, String> {
        val selectedFilter = hashMapOf<String, String>()
        for (filter in searchFilterList) {
            for (item in filter.filters) {
                if (item.isSelected) {
                    selectedFilter.put(filter.key, item.value)
                    break
                }
            }
        }
        return selectedFilter
    }
}
