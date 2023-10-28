package com.doubtnutapp.gamification.userProfileData.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class UserTodaysPointItemDataModel(
        val dailyPoint: String? = null) : Parcelable