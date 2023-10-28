package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class SimilarTopicBoosterEntity(
    val id: Int,
    val questionId: String,
    val questionTitle: String,
    var isSubmitted: Int,
    var submittedOption: String?,
    val options: List<SimilarTopicBoosterOptionEntity>,
    val widgetType: String,
    val submitUrlEndpoint: String,
    val headerImage: String,
    val resourceType: String,
    val backgroundColor: String,
    val solutionTextColor: String,
    val heading: String?
) : DoubtnutViewItem
