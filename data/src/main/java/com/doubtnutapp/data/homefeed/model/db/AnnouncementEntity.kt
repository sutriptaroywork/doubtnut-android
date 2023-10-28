package com.doubtnutapp.data.homefeed.model.db

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-08-01.
 */
@Keep
data class AnnouncementEntity(
    val type: String = "",
    val state: Boolean = false
)
