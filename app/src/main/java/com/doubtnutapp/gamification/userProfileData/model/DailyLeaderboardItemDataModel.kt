package com.doubtnutapp.gamification.userProfileData.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class DailyLeaderboardItemDataModel(
        val leaderBoardProfileImage: String = "",
        val leaderBoardUserId: Int = 0,
        val leaderBoardUserName: String = "",
        val leaderBoardRank: Int = 0,
        val leaderBoardPoints: String = "") : Parcelable