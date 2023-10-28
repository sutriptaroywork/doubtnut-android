package com.doubtnutapp.matchquestion.ui.viewholder

import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.databinding.ItemMatchPageFilterBinding
import com.doubtnutapp.matchquestion.listener.FilterDataListener
import com.doubtnutapp.matchquestion.listener.FilterTopicClickListener
import com.doubtnutapp.matchquestion.model.MatchFilterFacetViewItem
import com.doubtnutapp.matchquestion.model.MatchFilterTopicViewItem
import com.doubtnutapp.matchquestion.ui.adapter.MatchFilterTopicAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

/**
 * Created by Sachin Saxena on 2020-06-01.
 */
class MatchFragmentFilterViewHolder(
    private val binding: ItemMatchPageFilterBinding,
    private val filterDataListener: FilterDataListener
) : BaseViewHolder<MatchFilterFacetViewItem>(binding.root) {

    private var adapter: MatchFilterTopicAdapter? = null

    override fun bind(data: MatchFilterFacetViewItem) {

        binding.filterTitle.text = data.display
        setUpRecyclerView(data.data, data.isMultiSelect)
        binding.executePendingBindings()
    }

    private fun setUpRecyclerView(
        topicList: List<MatchFilterTopicViewItem>?,
        isMultiSelect: Boolean
    ) {

        if (topicList == null) return

        val layoutManager = FlexboxLayoutManager(binding.root.context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        binding.topicRecyclerView.layoutManager = layoutManager

        adapter = MatchFilterTopicAdapter(topicList, adapterPosition, true, isMultiSelect, false,
            topicClickListener = object : FilterTopicClickListener {
                override fun onTopicClick(
                    topicPosition: Int,
                    isSelected: Boolean,
                    toRefresh: Boolean,
                    facetPosition: Int,
                    isMultiSelect: Boolean
                ) {
                    adapter?.updateSelection(
                        topicPosition,
                        !isSelected,
                        isMultiSelect,
                        facetPosition
                    )
                    filterDataListener.onUpdate(
                        topicPosition,
                        !isSelected,
                        toRefresh,
                        facetPosition
                    )
                }
            })
        binding.topicRecyclerView.adapter = adapter
    }
}