package com.doubtnutapp.gamification.userProfileData.model

import androidx.annotation.Keep

@Keep
data class UserProfileDTO(
        val userProfileInfoDataModel: UserProfileInfoDataModel,
        val userProfileDataModel: UserProfileDataModel
)