package com.doubtnutapp.data.remote.models.doubtfeed

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 1/5/21.
 */

@Keep
data class Topic(
    @SerializedName("title") val title: String,
    @SerializedName("key") val key: String,
    var isSelected: Boolean = false,
)
