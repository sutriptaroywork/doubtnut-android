package com.doubtnutapp.domain.gamification.userProfile.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.gamification.userProfile.entity.*

@Keep
data class UserProfileEntity(
    val profileImage: String? = "",
    val leaderBoard: List<DailyLeaderboardItemEntity>?,
    val userRecentBadges: List<MyBadgesItemEntity>?,
    val dailyStreakProgress: List<DailyAttendanceEntity>?,
    val userLevel: String = "",
    val userLifetimePoints: String = "",
    val userTodaysPoint: String? = "",
    val userName: String? = "",
    val pointsToEarned: String = "",
    val userEmail: String? = "",
    val userSchoolName: String? = "",
    val userPincode: String? = "",
    val userCoaching: String? = "",
    val userDob: String? = "",
    val coins: Int = 0,
    val isLoggedIn: Boolean = false,
    val otherStats: List<UserOtherStats>,
    val bannerImage: String?,
    val studentClass: String?,
    val myBioEntity: MyBioEntity?,
    val subscriptionStatus: Boolean?,
    val subscriptionImageUrl: String?,
    val mobileNumber: String?,
    val countryCode: String?,
    val board: String?,
    val exams: List<String>?,
)
