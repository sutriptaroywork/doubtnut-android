package com.doubtnutapp.newglobalsearch.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.R
import com.doubtnutapp.base.IasFilterTypeDeselected
import com.doubtnutapp.base.IasFilterTypeSelected
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.newglobalsearch.ui.viewholder.SearchFilterTypeViewHolder

class SearchFilterTypeAdapter(private val actionPerformer: ActionPerformer) :
    RecyclerView.Adapter<SearchFilterTypeViewHolder>(), ActionPerformer {

    private var searchFilterList: ArrayList<SearchFilter> = arrayListOf()
    private var currentFilter = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchFilterTypeViewHolder {
        var viewHolder = SearchFilterTypeViewHolder(
            parent.context,
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ias_filter_type, parent, false),
            this
        )
        return viewHolder
    }

    override fun getItemCount(): Int = searchFilterList.size

    override fun onBindViewHolder(holder: SearchFilterTypeViewHolder, position: Int) {
        holder.bind(searchFilterList[position], position, currentFilter == position)
    }

    fun updateData(data: List<SearchFilter>, selectedPos: Int) {
        currentFilter = selectedPos
        if (!data.isNullOrEmpty()) {
            searchFilterList = data as ArrayList<SearchFilter>
            notifyItemChanged(0)
        } else {
            searchFilterList.clear()
            notifyItemRemoved(0)
        }
    }

    override fun performAction(action: Any) {
        if (action is IasFilterTypeSelected) {
            val old = currentFilter
            currentFilter = action.position
            notifyItemChanged(old)
            notifyItemChanged(currentFilter)
            actionPerformer.performAction(action)
        }
        if (action is IasFilterTypeDeselected) {
            actionPerformer.performAction(action)
        }
    }

}
