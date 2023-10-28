package com.doubtnutapp.domain.gamification.userProfile.entity

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-11-17.
 */
@Keep
data class MyBioEntity(
    val title: String?,
    val imageUrl: String?,
    val description: String?,
    val isAchieved: Int?,
    val blurImage: String?,
    val buttonText: String?
)
