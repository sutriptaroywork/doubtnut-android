package com.doubtnutapp.matchquestion.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.doubtnut.core.utils.setMargins
import com.doubtnutapp.R
import com.doubtnutapp.hide
import com.doubtnutapp.matchquestion.listener.FilterDataListener
import com.doubtnutapp.matchquestion.listener.FilterFacetClickListener
import com.doubtnutapp.matchquestion.listener.FilterTopicClickListener
import com.doubtnutapp.matchquestion.listener.MatchPageFilterListener
import com.doubtnutapp.matchquestion.model.MatchFilterFacetListViewItem
import com.doubtnutapp.matchquestion.model.MatchFilterFacetViewItem
import com.doubtnutapp.matchquestion.model.MatchFilterTopicViewItem
import com.doubtnutapp.matchquestion.ui.adapter.MatchFilterFacetAdapter
import com.doubtnutapp.matchquestion.ui.adapter.MatchFilterTopicAdapter
import com.doubtnutapp.show
import kotlinx.android.synthetic.main.item_match_filter_facet_list.view.*

class MatchFilterFacetListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private lateinit var matchPageFilterListener: MatchPageFilterListener
    private lateinit var filterDataListener: FilterDataListener

    private var matchFilerFacetAdapter: MatchFilterFacetAdapter? = null
    private var matchFilterTopicAdapter: MatchFilterTopicAdapter? = null

    private var selectedFacetPosition: Int = -1

    init {
        inflate(context, R.layout.item_match_filter_facet_list, this)
    }

    fun bindData(matchedQuestion: MatchFilterFacetListViewItem) {
        facetTitle.text = context.resources.getText(R.string.Refine_by)
        allTopics.text = context.resources.getText(R.string.all_filters)

        setUpFacetRecyclerView(matchedQuestion.facetList)

        allTopics.setOnClickListener {
            matchPageFilterListener.showMatchFilterFragment()
        }

        topicRecyclerView.hide()
        allTopics.hide()
        lastFacetTitleContainer.hide()
    }

    fun setMatchPageFilterListener(listener: MatchPageFilterListener) {
        matchPageFilterListener = listener
    }

    fun setFilterDataListener(listener: FilterDataListener) {
        filterDataListener = listener
    }

    private fun setUpFacetRecyclerView(facetList: List<MatchFilterFacetViewItem>) {

        if (facetList.isEmpty()) return

        selectedFacetPosition = facetList.indexOfFirst {
            it.isSelected
        }

        if (selectedFacetPosition == -1) {
            selectedFacetPosition = 0
            facetList[selectedFacetPosition].isSelected = false
        }

        matchFilerFacetAdapter =
            MatchFilterFacetAdapter(facetList, object : FilterFacetClickListener {

                override fun onFacetClick(
                    facetPosition: Int,
                    topicList: List<MatchFilterTopicViewItem>,
                    isMultiSelect: Boolean
                ) {
                    selectedFacetPosition = facetPosition
                    matchFilerFacetAdapter?.updateSelection(facetPosition)
                    topicRecyclerView.show()
                    allTopics.show()
                    setUpTopicRecyclerView(topicList, facetPosition, isMultiSelect)
                }

                override fun clearFilter() {
                    filterDataListener.clearFilter()
                }
            })

        facetRecyclerView.apply {
            adapter = matchFilerFacetAdapter
            smoothScrollToPosition(selectedFacetPosition)
        }

        setUpTopicRecyclerView(
            facetList[selectedFacetPosition].data,
            selectedFacetPosition,
            facetList[selectedFacetPosition].isMultiSelect
        )
    }

    private fun setUpTopicRecyclerView(
        topicList: List<MatchFilterTopicViewItem>?,
        facetPosition: Int,
        isMultiSelect: Boolean
    ) {

        if (topicList == null || topicList.isEmpty()) return

        lastFacetTitleContainer.show()

        val lastFacet = topicList[topicList.size - 1]
        setLastTopic(lastFacet, topicList.size - 1)

        val layoutParams = lastFacetTitle.layoutParams as MarginLayoutParams

        if (topicList.size == 1) {
            lastFacetTitle.setMargins(0, 28, 0, 0)
            lastFacetTitle.layoutParams = layoutParams
            topicRecyclerView.hide()
        } else {
            lastFacetTitle.setMargins(0, 7, 0, 0)
            lastFacetTitle.layoutParams = layoutParams
            val finalTopicList = topicList.subList(0, topicList.size - 1)
            topicRecyclerView.show()
            matchFilterTopicAdapter = MatchFilterTopicAdapter(
                finalTopicList,
                facetPosition,
                false,
                isMultiSelect,
                false,
                topicClickListener = object : FilterTopicClickListener {

                    override fun onTopicClick(
                        topicPosition: Int,
                        isSelected: Boolean,
                        toRefresh: Boolean,
                        facetPosition: Int,
                        isMultiSelect: Boolean
                    ) {
                        filterDataListener.onUpdate(
                            topicPosition,
                            !isSelected,
                            toRefresh,
                            selectedFacetPosition
                        )
                        matchFilterTopicAdapter?.updateSelection(
                            topicPosition,
                            !isSelected,
                            isMultiSelect,
                            selectedFacetPosition
                        )
                    }
                }
            )

            topicRecyclerView.adapter = matchFilterTopicAdapter
        }
    }

    private fun setLastTopic(lastTopic: MatchFilterTopicViewItem, topicPosition: Int) {
        lastFacetTitle.apply {
            text = lastTopic.display
            isEnabled = true
            isSelected = lastTopic.isSelected
            background = when {
                lastTopic.isSelected -> {
                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.fragment_match_filter_selected_text
                        )
                    )
                    ContextCompat.getDrawable(context, R.drawable.background_selected_filter)
                }
                else -> {
                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.fragment_match_filter_unselected_text
                        )
                    )
                    ContextCompat.getDrawable(context, R.drawable.background_unselected_filter)
                }
            }

            setOnClickListener {
                filterDataListener.onUpdate(
                    topicPosition,
                    !lastTopic.isSelected,
                    true,
                    selectedFacetPosition
                )
            }
        }
    }
}