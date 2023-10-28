package com.doubtnutapp.domain.gamification.dailyattendance.entity
import androidx.annotation.Keep

@Keep
data class DailyAttendanceEntity(
    val title: String,
    val titleImg: String,
    val heading: String,
    val dailyStreakEntity: List<CurrentStreakEntity>,
    val longestStreak: Int,
    val longestStreakImage: String
) {
    @Keep
    data class CurrentStreakEntity(
        val title: String,
        val icon: String,
        val isAchieved: Int,
        val type: String,
        val points: Int
    )
}
