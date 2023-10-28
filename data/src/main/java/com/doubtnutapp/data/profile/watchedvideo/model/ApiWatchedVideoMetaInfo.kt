package com.doubtnutapp.data.profile.watchedvideo.model

import com.google.gson.annotations.SerializedName

data class ApiWatchedVideoMetaInfo(

    @SerializedName("icon") val icon: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("Button") val suggestionButtonText: String,
    @SerializedName("id") val suggestionId: String,
    @SerializedName("playlist_name") val suggestionName: String

)
