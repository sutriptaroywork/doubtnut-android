package com.doubtnutapp.gamification.userProfileData.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class MyBadgesItemDataModel(
        val imageUrl: String = "",
        val name: String = "",
        val description: String = "",
        val id: Int = 0,
        val isAchieved: Boolean
) : Parcelable