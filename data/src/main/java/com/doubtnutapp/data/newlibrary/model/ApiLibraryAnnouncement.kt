package com.doubtnutapp.data.newlibrary.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiLibraryAnnouncement(
    @SerializedName("type") val type: String?,
    @SerializedName("state") val state: Boolean?
)
