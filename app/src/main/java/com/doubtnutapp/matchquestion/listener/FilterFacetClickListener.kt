package com.doubtnutapp.matchquestion.listener

import com.doubtnutapp.matchquestion.model.MatchFilterTopicViewItem

/**
 * Created by devansh on 05/09/20.
 */

interface FilterFacetClickListener {
    fun onFacetClick(
        facetPosition: Int,
        topicList: List<MatchFilterTopicViewItem>,
        isMultiSelect: Boolean
    )

    fun clearFilter()
}