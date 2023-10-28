package com.doubtnutapp.data.gamification.leaderboard.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiGameLeader(
    @SerializedName("profile_image")
    val profileImage: String?,
    @SerializedName("rank")
    val rank: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("points")
    val points: String,
    @SerializedName("is_own")
    val isOwn: Int = 0
)
