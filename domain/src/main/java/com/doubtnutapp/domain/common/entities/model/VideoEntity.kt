package com.doubtnutapp.domain.common.entities.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource

@Keep
data class VideoEntity(
    val questionId: String?,
    val autoPlay: Boolean,
    val viewId: String? = null,
    val videoResources: List<ApiVideoResource>?,
    val showFullScreen: Boolean?,
    val aspectRatio: String?
)
