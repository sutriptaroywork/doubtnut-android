package com.doubtnutapp.matchquestion.listener

interface FilterTopicClickListener {

    fun onTopicClick(
        topicPosition: Int,
        isSelected: Boolean,
        toRefresh: Boolean,
        facetPosition: Int,
        isMultiSelect: Boolean
    )
}