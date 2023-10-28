package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-10-14.
 */
@Keep
data class FilterInfo(
        val id: String,
        val title: String,
        val isLast: String?,
        var isSelected: Boolean = false)