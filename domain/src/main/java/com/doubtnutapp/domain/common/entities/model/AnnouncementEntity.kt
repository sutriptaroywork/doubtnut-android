package com.doubtnutapp.domain.common.entities.model

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-10-06.
 */
@Keep
data class AnnouncementEntity(
    val type: String = "",
    val state: Boolean = false
)
