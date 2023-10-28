package com.doubtnutapp.data.remote.models.topicboostergame2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 23/06/21.
 */

@Keep
data class Tab(
    @SerializedName("title") val title: String,
    @SerializedName("id") val id: Int,
)
