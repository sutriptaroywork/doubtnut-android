package com.doubtnutapp.gamification.userProfileData.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UserProfileDataModel(
        val leaderBoard: List<DailyLeaderboardItemDataModel>?,
        val dailyAttendance: List<DailyAttendanceDataModel>?,
        val myBadges: List<MyBadgesItemDataModel>?,
        val userTodaysPoint: String? = "",
        val userLifetimePoints: String = "",
        val coins: Int = 0,
        val userLevel: String = "",
        val myBio: MyBio?
) : Parcelable

