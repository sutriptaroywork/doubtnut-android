package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep

@Keep
data class ShowMoreViewItem(
    var status: Int,
    override val viewType: Int
) : MatchQuestionViewItem
