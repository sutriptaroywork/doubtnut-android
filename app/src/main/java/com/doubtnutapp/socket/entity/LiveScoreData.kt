package com.doubtnutapp.socket.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LiveScoreData(
    @SerializedName("team_one_score") val teamOneScore: String?,
    @SerializedName("team_two_score") val teamTwoScore: String?,
    @SerializedName("match_result") val matchResult: String?,
)