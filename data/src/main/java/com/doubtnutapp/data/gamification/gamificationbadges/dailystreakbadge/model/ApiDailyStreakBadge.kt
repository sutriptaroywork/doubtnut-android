package com.doubtnutapp.data.gamification.gamificationbadges.dailystreakbadge.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by shrreya on 28/6/19.
 */
@Keep
data class ApiDailyStreakBadge(
    @SerializedName("description")
    val description: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("is_achieved")
    val isAchieved: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("share_message")
    val sharingMessage: String
)
