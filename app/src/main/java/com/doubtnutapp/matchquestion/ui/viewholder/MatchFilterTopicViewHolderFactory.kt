package com.doubtnutapp.matchquestion.ui.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import com.doubtnutapp.R
import com.doubtnutapp.matchquestion.listener.FilterTopicClickListener

/**
 * Created by devansh on 09/09/20.
 */

class MatchFilterTopicViewHolderFactory(private val topicClickListener: FilterTopicClickListener) {

    fun getViewHolderFor(parent: ViewGroup, data: ViewHolderData): MatchFilterTopicBaseViewHolder =
        if (data.isUpperFocused) {
            MatchFilterTopicV2ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_match_filter_topic_v2, parent, false
                ),
                data.facetPosition, data.isFromFragment, data.isMultiSelect, data.questionId,
                topicClickListener
            )
        } else {
            MatchFilterTopicViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_match_filter_topic, parent, false
                ),
                data.facetPosition, data.isFromFragment, data.isMultiSelect,
                topicClickListener
            )
        }

    data class ViewHolderData(
        val facetPosition: Int = 0,
        val isFromFragment: Boolean = false,
        val isMultiSelect: Boolean = false,
        val isUpperFocused: Boolean = false,
        val questionId: String = ""
    )
}