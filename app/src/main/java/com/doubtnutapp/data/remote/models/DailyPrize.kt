package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class DailyPrize(
    @SerializedName("header") val headerDetails: HeaderDetails,
    @SerializedName("stats") val userDetails: UserDetails,
    @SerializedName("current_winner_list") val todayUsers: ArrayList<TodayUsers>,
    @SerializedName("previous_winner_list") val lastDayUser: ArrayList<LastDayUser>,
    @SerializedName("rules") val contestRules: ArrayList<ContestRules>
)
