package com.doubtnutapp.gamification.userProfileData.model

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class DailyAttendanceDataModel(
        val isAchieved: Boolean = false,
        val itemIcon: String = "",
        val title: String = "",
        val viewType: String,
        val points: Int = 0,
        @ColorInt val subtitleTextColor: Int = Color.BLACK
) : Parcelable