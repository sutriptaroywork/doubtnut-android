package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class SimilarTopicSearchEntity(
    val resourceType: String,
    val description: String?,
    val imageUrl: String?,
    val buttonText: String?,
    val buttonBgColor: String?,
    val searchText: String?
) : DoubtnutViewItem {
    companion object {
        const val resourceType: String = "ias_search_bar"
    }
}
