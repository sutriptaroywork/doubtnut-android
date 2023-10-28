package com.doubtnutapp.data.newlibrary.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiNewLibraryPlayList(
    @SerializedName("id") val playListId: String,
    @SerializedName("name") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("is_first") val isFirst: String,
    @SerializedName("is_Last") val isLast: String
)
