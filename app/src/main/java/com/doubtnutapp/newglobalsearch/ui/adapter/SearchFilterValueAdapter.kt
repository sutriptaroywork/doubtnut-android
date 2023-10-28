package com.doubtnutapp.newglobalsearch.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R

import com.doubtnutapp.base.IasFilterValueDeselected
import com.doubtnutapp.base.IasFilterValueSelected
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.viewholder.SearchAllFilterViewHolder
import com.doubtnutapp.newglobalsearch.ui.viewholder.SearchFilterValueViewHolder

class SearchFilterValueAdapter(
    private val actionPerformer: ActionPerformer,
    var facet: SearchTabsItem,
    private val isYoutube: Boolean,
    val isFromAllChapters: Boolean,
    private val showAllFilterAction: Boolean = true
) :
    RecyclerView.Adapter<SearchFilterValueAdapter.IASFilterValueViewHolder>(),
    ActionPerformer {

    private val TYPE_ITEM = 0
    private val TYPE_FOOTER = 1

    private var filterValues: ArrayList<SearchFilterItem> = arrayListOf()
    private var filterTypePosition = 0
    private var selectedFilterValue = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IASFilterValueViewHolder {
        if (viewType == TYPE_FOOTER) {
            return SearchAllFilterViewHolder(
                parent.context,
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.ias_all_filter_item, parent, false),
                facet,
                this,
                isYoutube
            )
        }
        return SearchFilterValueViewHolder(
            parent.context,
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.ias_filter_value_item,
                parent,
                false
            ),
            this,
            isYoutube,
            isFromAllChapters
        )
    }

    override fun getItemCount(): Int {
        var filterArrSize = filterValues.size
        return if (showAllFilterAction) {
            facet.filterList?.let {
                if ("chapter".equals(
                        facet.filterList!![filterTypePosition].key,
                        true
                    ) && filterValues.size > 4
                ) {
                    filterArrSize = 4
                }
            }
            filterArrSize + 1
        } else filterArrSize
    }

    override fun getItemViewType(position: Int): Int {
        if (showAllFilterAction) {
            var filterArrSize = filterValues.size
            facet.filterList?.let {
                if ("chapter".equals(
                        facet.filterList!![filterTypePosition].key,
                        true
                    ) && filterValues.size > 4
                ) {
                    filterArrSize = 4
                }
            }
            return if (position == filterArrSize) {
                TYPE_FOOTER
            } else {
                TYPE_ITEM
            }
        }
        return TYPE_ITEM
    }

    override fun onBindViewHolder(holder: IASFilterValueViewHolder, position: Int) {
        if (holder is SearchFilterValueViewHolder && position < filterValues.size) {
            val isChapter = "chapter".equals(facet.filterList!![filterTypePosition].key, true)
            holder.bind(
                filterValues[position],
                position,
                filterTypePosition,
                (!showAllFilterAction && isChapter)
            )
        }
        if (holder is SearchAllFilterViewHolder) {
            holder.bind()
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

        actionPerformer.performAction(action)
    }

    open class IASFilterValueViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }
}