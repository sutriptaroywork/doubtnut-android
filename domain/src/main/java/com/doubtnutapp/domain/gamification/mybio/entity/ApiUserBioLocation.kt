package com.doubtnutapp.domain.gamification.mybio.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiUserBioLocation(
    @SerializedName("location") val location: String?,
    @SerializedName("lat") val lat: String?,
    @SerializedName("lon") val lon: String?
)
