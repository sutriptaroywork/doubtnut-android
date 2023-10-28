package com.doubtnutapp.data.remote.models.quiztfs

import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 26-08-2021
 */
data class LiveQuestionsMedium(
    @SerializedName("title") val title: String,
    @SerializedName("key") val key: String,
    @SerializedName("is_selected") val isSelected: Boolean
)
