package com.doubtnutapp.data.gamification.gamificationbadges.userbadges.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiBadgeProgress(
    @SerializedName("requirements") val requirements: List<ApiRequirementsItem>?,
    @SerializedName("nudge_description") val nudgeDescription: String = "",
    @SerializedName("image_url") val imageUrl: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("description") val description: String = ""
)
