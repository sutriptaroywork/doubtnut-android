package com.doubtnutapp.data.gamification.userProfile.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiUserRecentBadgesItem(
    @SerializedName("image_url") val imageUrl: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("id") val badgesId: Int = 0,
    @SerializedName("is_achieved") val isAchieved: Int = 0
)
