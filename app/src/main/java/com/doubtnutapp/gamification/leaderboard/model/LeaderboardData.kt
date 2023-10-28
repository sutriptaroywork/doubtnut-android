package com.doubtnutapp.gamification.leaderboard.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class LeaderboardData (
        val allLeaderboardData: List<GameLeader>,
        val dailyLeaderboardData: List<GameLeader>
) : Parcelable