package com.doubtnutapp.domain.gamification.userProfile.entity

import androidx.annotation.Keep

@Keep
data class UserOtherStats(
    val id: Int = 0,
    val action: String = "",
    val action_display: String = "",
    val xp: Int,
    val is_active: Int = 0,
    val created_at: String = "",
    val actionPage: String = "",
    val count: Int = 0,
    val activity: String = ""

)
