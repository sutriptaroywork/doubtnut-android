package com.doubtnutapp.data.gamification.gamificationbadges.userbadges.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiUserBadge(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("nudge_description")
    val nudgeDescription: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("is_achieved")
    val isAchieved: Int,
    @SerializedName("share_message")
    val sharingMessage: String,
    @SerializedName("action_page")
    val actionPage: String?,
    @SerializedName("blur_image")
    val blurImage: String?

)
