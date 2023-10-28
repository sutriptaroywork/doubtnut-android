package com.doubtnutapp.domain.resourcelisting.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.common.entities.model.VideoEntity

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class QuestionMetaEntity(
    val questionId: String,
    val ocrText: String?,
    val question: String,
    val videoClass: String?,
    val microConcept: String?,
    val questionThumbnailImage: String?,
    val bgColorSimilar: String?,
    val doubtField: String?,
    val videoDuration: Int,
    val shareCount: Int,
    val likeCount: Int,
    val isLiked: Boolean,
    val sharingMessage: String?,
    val views: String?,
    val questionMeta: String?,
    val resourceType: String,
    val videoObj: VideoEntity?
) : RecyclerDomainItem {
    companion object {
        const val resourceType: String = "video"
    }
}
