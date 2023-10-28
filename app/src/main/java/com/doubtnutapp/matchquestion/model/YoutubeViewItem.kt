package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep

@Keep
data class YoutubeViewItem(
    val thumbnail: ApiThumbnails,
    val duration: String,
    val title: String,
    val youtubeId: String,
    val description: String,
    override val viewType: Int
) : MatchQuestionViewItem