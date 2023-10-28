package com.doubtnutapp.domain.gamification.mybio.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiUserBioCoaching(
    @SerializedName("active") val isActive: String?,
    @SerializedName("name") val name: String?
)
