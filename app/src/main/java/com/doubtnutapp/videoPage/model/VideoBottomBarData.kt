package com.doubtnutapp.videoPage.model

import androidx.annotation.Keep

/**
 * Created by devansh on 17/4/21.
 */

@Keep
data class VideoBottomBarData(
        val questionId: String,
        val questionMeta: String?,
        val ocrText: String?,
        val position: Int,
)