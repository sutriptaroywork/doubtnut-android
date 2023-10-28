package com.doubtnutapp.gamification.userProfileData.model

import androidx.annotation.Keep


@Keep
data class UserProfileInfoDataModel(

        val profileImage: String? = "",
        val userName: String = "",
        val isLoggedIn: Boolean = false,
        val pointsToEarned : String = ""
        )