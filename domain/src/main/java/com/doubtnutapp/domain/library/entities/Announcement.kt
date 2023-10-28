package com.doubtnutapp.domain.library.entities

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-07-31.
 */
@Keep
data class Announcement(
    val type: String = "",
    val state: Boolean = false
)
