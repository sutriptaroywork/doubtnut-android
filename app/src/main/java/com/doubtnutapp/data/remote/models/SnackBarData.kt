package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class SnackBarData(
    @SerializedName("bg_color")
    val bgColor: String?,

    @SerializedName("icon_url")
    val iconUrl: String?,
    @SerializedName("next_interval")
    val nextInterval: Long?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_color")
    val titleColor: String?,
    @SerializedName("title_size")
    val titleSize: String?,

    @SerializedName("action_title")
    val deeplinkText: String?,
    @SerializedName("action_color")
    val deeplinkTextColor: String?,
    @SerializedName("action_size")
    val deeplinkTextSize: String?,
    @SerializedName("action_deeplink")
    val deeplink: String?,
)
