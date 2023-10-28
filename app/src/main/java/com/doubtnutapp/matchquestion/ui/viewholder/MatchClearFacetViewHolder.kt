package com.doubtnutapp.matchquestion.ui.viewholder

import android.view.View
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemMatchClearFilterBinding
import com.doubtnutapp.matchquestion.listener.FilterFacetClickListener
import com.doubtnutapp.matchquestion.model.MatchFilterFacetViewItem

/**
 * Created by Sachin Saxena on 2020-06-03.
 */
class MatchClearFacetViewHolder(
    private val containerView: View,
    private val facetClickListener: FilterFacetClickListener
) : BaseViewHolder<MatchFilterFacetViewItem>(containerView) {

    val binding = ItemMatchClearFilterBinding.bind(itemView)

    override fun bind(data: MatchFilterFacetViewItem) {
        binding.clearFilter.apply {
            setOnClickListener {
                facetClickListener.clearFilter()
            }
        }
    }
}