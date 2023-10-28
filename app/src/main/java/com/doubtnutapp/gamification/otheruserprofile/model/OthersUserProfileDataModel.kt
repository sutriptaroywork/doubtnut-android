package com.doubtnutapp.gamification.otheruserprofile.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.gamification.userProfileData.model.DailyAttendanceDataModel
import com.doubtnutapp.gamification.userProfileData.model.DailyLeaderboardItemDataModel
import com.doubtnutapp.gamification.userProfileData.model.MyBadgesItemDataModel
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class OthersUserProfileDataModel(

        val profileImage: String? = "",
        val leaderBoard: List<DailyLeaderboardItemDataModel>?,
        val userRecentBadges: List<MyBadgesItemDataModel>?,
        val dailyStreakProgress: List<DailyAttendanceDataModel>?,
        val userLevel: String = "",
        val userLifetimePoints: String = "0",
        val userTodaysPoint: String? = "0",
        val userName: String = "",
        val isLoggedIn: Boolean = false,
        val otherUserStats: List<OtherUserStatsDataModel>,
        val bannerImg: String?,
        val studentClass: String?
): Parcelable