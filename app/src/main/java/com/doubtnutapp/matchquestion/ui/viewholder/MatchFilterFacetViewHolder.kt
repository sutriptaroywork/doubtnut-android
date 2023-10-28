package com.doubtnutapp.matchquestion.ui.viewholder

import android.view.View
import androidx.core.content.ContextCompat
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemMatchFilterFacetBinding
import com.doubtnutapp.matchquestion.listener.FilterFacetClickListener
import com.doubtnutapp.matchquestion.model.MatchFilterFacetViewItem

/**
 * Created by Sachin Saxena on 2020-06-03.
 */
class MatchFilterFacetViewHolder(
    private val containerView: View,
    private val facetClickListener: FilterFacetClickListener
) : BaseViewHolder<MatchFilterFacetViewItem>(containerView) {

    val binding = ItemMatchFilterFacetBinding.bind(containerView)

    override fun bind(data: MatchFilterFacetViewItem) {
        binding.facetTitle.apply {
            text = data.display
            background = if (data.isSelected) {
                ContextCompat.getDrawable(context, R.drawable.background_selected_filter)
            } else {
                ContextCompat.getDrawable(context, R.drawable.background_unselected_filter)
            }
            setOnClickListener {
                facetClickListener.onFacetClick(adapterPosition, data.data, data.isMultiSelect)
            }
        }
    }
}