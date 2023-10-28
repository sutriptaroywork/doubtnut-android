package com.doubtnutapp.data.gamification.userProfile.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiLeaderboardItem(
    @SerializedName("profile_image") val leaderBoardProfileImage: String?,
    @SerializedName("user_id") val leaderBoardUserId: Int = 0,
    @SerializedName("user_name") val leaderBoardUserName: String = "",
    @SerializedName("rank") val leaderBoardRank: Int = 0,
    @SerializedName("points") val leaderBoardPoints: Int = 0
)
