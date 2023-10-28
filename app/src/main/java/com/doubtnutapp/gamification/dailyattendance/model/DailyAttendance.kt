package com.doubtnutapp.gamification.dailyattendance.model

import androidx.annotation.Keep

@Keep
data class DailyAttendanceDataModel(

        val title: String,
        val titleImg: String,
        val heading: String,
        val dailyStreak: List<CurrentStreakDataModel>,
        val longestStreak: Int,
        val longestStreakImage: String
) {
    @Keep
    data class CurrentStreakDataModel(
            val title: String,
            val icon: String,
            val isAchieved: Int,
            val type: String,
            val points: Int
    )
}