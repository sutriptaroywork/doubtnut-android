package com.doubtnutapp.matchquestion.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.matchquestion.listener.FilterFacetClickListener
import com.doubtnutapp.matchquestion.model.MatchFilterFacetViewItem
import com.doubtnutapp.matchquestion.ui.viewholder.MatchClearFacetViewHolder
import com.doubtnutapp.matchquestion.ui.viewholder.MatchFilterFacetViewHolder

/**
 * Created by Sachin Saxena on 2019-12-02.
 */
class MatchFilterFacetAdapter(
    private var facetList: List<MatchFilterFacetViewItem>,
    private val facetClickListener: FilterFacetClickListener
) : RecyclerView.Adapter<BaseViewHolder<MatchFilterFacetViewItem>>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<MatchFilterFacetViewItem> {
        if (viewType == R.layout.item_match_filter_facet) {
            return MatchFilterFacetViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_match_filter_facet,
                    parent,
                    false
                ),
                facetClickListener
            )
        }
        return MatchClearFacetViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_match_clear_filter,
                parent,
                false
            ),
            facetClickListener
        )

    }

    override fun getItemViewType(position: Int): Int {
        return facetList[position].viewType
    }

    override fun getItemCount() = facetList.size

    override fun onBindViewHolder(
        holderAdvancedSearch: BaseViewHolder<MatchFilterFacetViewItem>,
        position: Int
    ) {
        holderAdvancedSearch.bind(facetList[position])
    }

    fun updateSelection(position: Int) {
        facetList.forEachIndexed { index, _ ->
            if (index == position) facetList[position].isSelected = true
            else {
                val facet = facetList[index].data.find {
                    it.isSelected
                }
                facetList[index].isSelected = facet != null
            }
        }

        notifyDataSetChanged()
    }

}