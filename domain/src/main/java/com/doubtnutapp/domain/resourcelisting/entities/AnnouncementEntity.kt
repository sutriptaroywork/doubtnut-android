package com.doubtnutapp.domain.resourcelisting.entities

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class AnnouncementEntity(
    val type: String = "",
    val state: Boolean = false
)
