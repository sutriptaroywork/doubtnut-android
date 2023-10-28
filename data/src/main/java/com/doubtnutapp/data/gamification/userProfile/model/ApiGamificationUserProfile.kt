package com.doubtnutapp.data.gamification.userProfile.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiGamificationUserProfile(

    @SerializedName("profile_image")
    val profileImage: String? = "",

    @SerializedName("leaderboard")
    val leaderBoard: List<ApiDailyLeaderboardItem>?,

    @SerializedName("user_recent_badges")
    val userRecentBadges: List<ApiMyBadgesItem>?,

    @SerializedName("user_lifetime_points")
    val userLifetimePoints: Int?,

    @SerializedName("daily_streak_progress")
    val dailyStreakProgress: List<ApiDailyAttendanceItem>?,

    @SerializedName("user_level")
    val userLevel: String = "",

    @SerializedName("user_todays_point")
    val userTodaysPoint: Int?,

    @SerializedName("username")
    val userName: String? = "",

    @SerializedName("points_to_earned_with_login")
    val pointsToEarned: String = "",

    @SerializedName("student_email")
    val userEmail: String? = "",

    @SerializedName("school_name")
    val userSchoolName: String? = "",

    @SerializedName("pincode")
    val userPincode: String? = "",

    @SerializedName("coaching")
    val userCoaching: String? = "",

    @SerializedName("dob")
    val userDob: String? = "",

    @SerializedName("banner_img")
    val bannerImg: String? = "",

    @SerializedName("coins")
    val coins: Int = 0,

    @SerializedName("is_own")
    val isOwn: Int = 0,

    @SerializedName("others_stats")
    val othersStats: List<ApiOtherStats>?,

    @SerializedName("class")
    val studentClass: String?,

    @SerializedName("completeness")
    val completeness: ApiMyBio?,

    @SerializedName("subscription_status")
    val subscriptionStatus: Boolean?,

    @SerializedName("subscription_image")
    val subscriptionImageUrl: String?,

    @SerializedName("mobile")
    val mobileNumber: String?,

    @SerializedName("country_code")
    val countryCode: String?,

    @SerializedName("board")
    val board: String?,

    @SerializedName("exams")
    val exams: List<String>?,
)
