package com.doubtnutapp.data.gamification.gamificationbadges.userbadges.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiFriendsBadge(
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("is_achieved")
    val isAchieved: Int,
    @SerializedName("name")
    val name: String
)
