package com.doubtnutapp.matchquestion.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.matchquestion.listener.FilterTopicClickListener
import com.doubtnutapp.matchquestion.model.MatchFilterTopicViewItem
import com.doubtnutapp.matchquestion.ui.viewholder.MatchFilterTopicViewHolderFactory
import com.doubtnutapp.matchquestion.ui.viewholder.MatchFilterTopicBaseViewHolder

/**
 * Created by Sachin Saxena on 2020-06-01.
 */
class MatchFilterTopicAdapter(
    private var topicList: List<MatchFilterTopicViewItem>,
    private var facetPosition: Int,
    private val isFromFragment: Boolean,
    private val isMultiSelect: Boolean,
    private val isUpperFocused: Boolean = false,
    private val questionId: String = "",
    private val topicClickListener: FilterTopicClickListener
) : RecyclerView.Adapter<MatchFilterTopicBaseViewHolder>() {

    private val viewHolderFactory: MatchFilterTopicViewHolderFactory by lazy {
        MatchFilterTopicViewHolderFactory(topicClickListener)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MatchFilterTopicBaseViewHolder =
        viewHolderFactory.getViewHolderFor(
            parent,
            MatchFilterTopicViewHolderFactory.ViewHolderData(
                facetPosition = facetPosition,
                isFromFragment = isFromFragment,
                isMultiSelect = isMultiSelect,
                isUpperFocused = isUpperFocused,
                questionId = questionId
            )
        )

    override fun getItemCount() = topicList.size

    override fun onBindViewHolder(
        holderAdvancedSearch: MatchFilterTopicBaseViewHolder,
        position: Int
    ) {
        holderAdvancedSearch.bind(topicList[position])
    }

    fun updateSelection(
        position: Int,
        isSelected: Boolean,
        isMultiSelect: Boolean,
        facetPosition: Int
    ) {
        this.facetPosition = facetPosition
        if (isMultiSelect) {
            topicList[position].isSelected = isSelected
        } else {
            if (isSelected) {
                topicList.forEachIndexed { index, matchFilterTopic ->
                    topicList[index].isSelected = position == index
                }
            } else {
                topicList[position].isSelected = false
            }

        }
        notifyDataSetChanged()
    }
}