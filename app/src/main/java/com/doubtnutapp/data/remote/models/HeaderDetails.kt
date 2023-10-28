package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class HeaderDetails(
    @SerializedName("headline") val headline: String,
    @SerializedName("description") val description: String,
    @SerializedName("bg_color") val bgColor: String,
    @SerializedName("logo") val contestLogo: String,
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("amount") val amount: Int

)
