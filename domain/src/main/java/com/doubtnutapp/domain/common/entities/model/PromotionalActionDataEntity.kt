package com.doubtnutapp.domain.common.entities.model

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
@Keep
data class PromotionalActionDataEntity(
    val playlistId: String,
    val playlistTitle: String,
    val isLast: Int,
    val facultyId: Int?,
    val ecmId: Int?,
    val subject: String?
)
