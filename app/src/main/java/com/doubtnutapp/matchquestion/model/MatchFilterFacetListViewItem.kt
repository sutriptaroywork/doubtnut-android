package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep

@Keep
data class MatchFilterFacetListViewItem(
    override val viewType: Int,
    val facetList: List<MatchFilterFacetViewItem>,
    val displayFilter: Boolean
) : MatchQuestionViewItem