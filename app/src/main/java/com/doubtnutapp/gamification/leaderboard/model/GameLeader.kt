package com.doubtnutapp.gamification.leaderboard.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class GameLeader(
        val profileImage: String?,
        val rank: Int,
        val userId: Int,
        val userName: String,
        val points : String,
        val isOwn: Boolean

) : Parcelable