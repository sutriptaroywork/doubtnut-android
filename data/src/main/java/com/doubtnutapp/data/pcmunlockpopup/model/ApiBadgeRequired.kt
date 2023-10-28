package com.doubtnutapp.data.pcmunlockpopup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiBadgeRequired(
    @SerializedName("current_progress")
    val currentProgress: Int,
    @SerializedName("description")
    val description: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("nudge_description")
    val nudgeDescription: String,
    @SerializedName("requirement")
    val requirement: Int,
    @SerializedName("requirement_type")
    val requirementType: String
)
