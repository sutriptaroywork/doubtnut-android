package com.doubtnutapp.data.resourcelisting.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class ApiPlayListMetaInfo(
    @SerializedName("icon") val icon: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = "",
    @SerializedName("Button") val suggestionButtonText: String?,
    @SerializedName("id") val suggestionId: String?,
    @SerializedName("playlist_name") val suggestionName: String?
)
