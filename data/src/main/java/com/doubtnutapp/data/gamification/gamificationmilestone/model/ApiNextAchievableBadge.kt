package com.doubtnutapp.data.gamification.gamificationmilestone.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiNextAchievableBadge(
    @SerializedName("description")
    val description: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("next_text")
    val nextText: String
)
