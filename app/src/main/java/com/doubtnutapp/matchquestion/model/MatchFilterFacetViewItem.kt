package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.google.gson.JsonArray

@Keep
data class MatchFilterFacetViewItem(

    val facetType: String,

    val display: String,

    val local: Boolean,

    var isSelected: Boolean,

    val isMultiSelect: Boolean,

    val showDisplayText: Boolean,

    val isUpperFocused: Boolean,

    var data: List<MatchFilterTopicViewItem>,

    override val viewType: Int

) : MatchQuestionViewItem

@Keep
data class MatchFilterTopicViewItem(

    val display: String,

    var isSelected: Boolean,

    val isAllTopic: Boolean,

    val type: String?,

    val data: JsonArray,

    val selectable: Boolean?
)

