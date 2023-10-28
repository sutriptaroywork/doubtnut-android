package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class SimilarVideoEntity(
    val matchedQuestions: List<DoubtnutViewItem>,
    val conceptVideos: List<DoubtnutViewItem>?,
    val feedback: FeedBackSimilarVideoEntity?
)
