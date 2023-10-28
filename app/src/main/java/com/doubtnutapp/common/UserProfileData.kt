package com.doubtnutapp.common

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-11-17.
 */
@Keep
data class UserProfileData(
    val name: String,
    val level: String,
    val profileImage: String,
    val bannerImage: String,
    val subscriptionStatus: Boolean?,
    val subscriptionImageUrl: String?
)
