package com.doubtnutapp.data.gamification.userProfile.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiDailyAttendanceItem(

    @SerializedName("is_achieved")
    val isAchieved: Int = 0,

    @SerializedName("icon")
    val itemIcon: String = "",

    @SerializedName("type")
    val badgeType: String = "",

    @SerializedName("title")
    val itemTitle: String = "",

    @SerializedName("points")
    val points: Int = 0

)
