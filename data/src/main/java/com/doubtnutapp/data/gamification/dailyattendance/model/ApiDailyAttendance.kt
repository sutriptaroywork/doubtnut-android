package com.doubtnutapp.data.gamification.dailyattendance.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiDailyAttendance(

    @SerializedName("title")
    val title: String?,

    @SerializedName("title_img")
    val titleImg: String?,

    @SerializedName("heading")
    val heading: String?,

    @SerializedName("daily_streak")
    val dailyStreak: List<ApiCurrentStreak>?,

    @SerializedName("longest_streak")
    val longestStreak: Int?,

    @SerializedName("longest_streak_image")
    val longestStreakImage: String?
) {
    @Keep
    data class ApiCurrentStreak(

        @SerializedName("title")
        val title: String?,

        @SerializedName("icon")
        val icon: String?,

        @SerializedName("is_achieved")
        val isAchieved: Int?,

        @SerializedName("type")
        val type: String?,

        @SerializedName("points")
        val points: Int?
    )
}
