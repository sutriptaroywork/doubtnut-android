package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep

/**
 * Created by Sachin Saxena on 2019-12-09.
 */
@Keep
data class MatchQuestionBanner(
    val content: String,
    val dnCash: Int?
)