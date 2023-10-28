package com.doubtnutapp.data.gamification.userProfile.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiMyBadgesItem(

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("nudge_description")
    val nudgeDescription: String?,

    @SerializedName("id")
    val badgesId: Int?,

    @SerializedName("is_active")
    val isActive: Int?,

    @SerializedName("visible_for")
    val visibleFor: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("banner_img")
    val bannerImg: String?,

    @SerializedName("requirement")
    val requirement: Int?,

    @SerializedName("requirement_type")
    val requirementType: String?,

    @SerializedName("is_achieved")
    val isAchieved: Int?,

    @SerializedName("blur_image")
    val blurImage: String?
)
