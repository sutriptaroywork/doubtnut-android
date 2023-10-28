package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class SimilarTopicBoosterOptionEntity(
    val optionCode: String,
    val optionTitle: String,
    val isAnswer: Int,
    var optionStatus: Int
) : DoubtnutViewItem {
    companion object {
        const val resourceType: String = "video"
        const val type: String = "TOPIC_BOOSTER_ITEM"
    }
}
