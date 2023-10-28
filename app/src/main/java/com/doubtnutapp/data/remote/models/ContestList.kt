package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class ContestList(
    @SerializedName("id") val contestId: String,
    @SerializedName("type") val contestType: String,
    @SerializedName("parameter") val contestParameter: String,
    @SerializedName("headline") val contestHeadline: String,
    @SerializedName("logo") val contestLogo: String,
    @SerializedName("bg_color") val contestBgColor: String,
    @SerializedName("description") val contestDescription: String,
    @SerializedName("winner_count") val contestWinnerCount: String,
    @SerializedName("date_from") val contestDateFom: String,
    @SerializedName("date_till") val contestDateTill: String

)
