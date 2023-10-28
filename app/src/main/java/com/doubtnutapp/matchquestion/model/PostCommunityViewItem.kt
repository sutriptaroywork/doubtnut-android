package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep

@Keep
data class PostCommunityViewItem(
    val title: Int,
    val buttonText: Int,
    val bgColor: String?,
    override val viewType: Int
) : MatchQuestionViewItem