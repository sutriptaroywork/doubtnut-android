package com.doubtnutapp.matchquestion.listener

interface FilterDataListener {
    fun onUpdate(
        topicPosition: Int,
        isTopicSelected: Boolean,
        toRefresh: Boolean,
        facetPosition: Int
    )

    fun clearFilter() {}
}