package com.doubtnutapp.matchquestion.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.matchquestion.listener.FilterDataListener
import com.doubtnutapp.matchquestion.model.MatchFilterFacetViewItem
import com.doubtnutapp.matchquestion.ui.viewholder.MatchFragmentFilterViewHolder

/**
 * Created by Sachin Saxena on 2020-06-01.
 */
class MatchFilterAdapter(private val filterDataListener: FilterDataListener) :
    RecyclerView.Adapter<MatchFragmentFilterViewHolder>() {

    private var facetList = mutableListOf<MatchFilterFacetViewItem>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MatchFragmentFilterViewHolder {
        return MatchFragmentFilterViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_match_page_filter,
                parent,
                false
            ),
            filterDataListener
        )
    }

    override fun getItemCount() = facetList.size

    override fun onBindViewHolder(
        holderAdvancedSearchFragment: MatchFragmentFilterViewHolder,
        position: Int
    ) {
        holderAdvancedSearchFragment.bind(facetList[position])
    }

    fun updateFilter(filterList: List<MatchFilterFacetViewItem>) {
        this.facetList.clear()
        this.facetList.addAll(filterList)
        notifyDataSetChanged()
    }
}