package com.doubtnutapp.newglobalsearch.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.IasClearAllFilters
import com.doubtnutapp.base.IasFilterSelected
import com.doubtnutapp.databinding.ItemIasAllFiltersBinding
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem

class IasAllFilterAdapter(
    private val actionPerformer: ActionPerformer,
    private val isYoutube: Boolean
) :
    RecyclerView.Adapter<IasAllFilterAdapter.AllFilterItemViewHolder>(), ActionPerformer {

    private var searchFilterList: ArrayList<SearchFilter> = arrayListOf()
    private lateinit var facet: SearchTabsItem

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllFilterItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ias_all_filters, parent, false)
        return AllFilterItemViewHolder(parent.context, view, this, facet)
    }

    override fun getItemCount(): Int = searchFilterList.size

    override fun onBindViewHolder(holder: AllFilterItemViewHolder, position: Int) {
        holder.bind(searchFilterList[position], position)
    }

    fun updateData(tab: SearchTabsItem, data: List<SearchFilter>) {
        facet = tab
        if (!data.isNullOrEmpty()) {
            searchFilterList = data as ArrayList<SearchFilter>
        } else {
            searchFilterList.clear()
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
            actionPerformer.performAction(IasFilterSelected(facet!!, hashMapOf(), isYoutube))
            notifyDataSetChanged()
        } else {
            actionPerformer.performAction(action)
        }
    }

    class AllFilterItemViewHolder(
        val context: Context,
        val view: View,
        val actionPerformer: ActionPerformer,
        val facet: SearchTabsItem
    ) : RecyclerView.ViewHolder(view) {

        val binding = ItemIasAllFiltersBinding.bind(view)

        fun bind(filter: SearchFilter, position: Int) {
            if (position % 2 == 0) {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_e0eaff))
            } else {
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            binding.filterHeading.text = filter.label

            binding.rvFilterValues.layoutManager =
                ChipsLayoutManager.newBuilder(context)
                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
                    .build()
            binding.rvFilterValues.itemAnimator = null
            val valueAdapter = SearchFilterValueAdapter(actionPerformer, facet, false, false, false)

            binding.rvFilterValues.adapter = valueAdapter
            valueAdapter.updateData(filter.filters, position, facet)
        }
    }
}
