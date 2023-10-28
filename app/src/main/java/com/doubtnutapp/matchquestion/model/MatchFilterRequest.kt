package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep

@Keep
data class MatchFilterRequest(
    val facets_v2: List<ApiAdvanceSearchData>,
    val ocr_text: String,
    val question_id: String,
    val source: String
)

