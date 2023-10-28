package com.doubtnutapp.data.gamification.userProfile.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiOtherStats(

    @SerializedName("id")
    val id: Int,

    @SerializedName("action")
    val action: String?,

    @SerializedName("action_display")
    val actionDisplay: String?,

    @SerializedName("xp")
    val xp: Int,

    @SerializedName("is_active")
    val isActive: Int,

    @SerializedName("action_page")
    val actionPage: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("count")
    val count: Int?,

    @SerializedName("activity")
    val activity: String?
)
