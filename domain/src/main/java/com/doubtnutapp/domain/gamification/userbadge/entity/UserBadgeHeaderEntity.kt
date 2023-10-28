package com.doubtnutapp.domain.gamification.userbadge.entity

import androidx.annotation.Keep

@Keep
data class UserBadgeHeaderEntity(
    val title: String,
    override val type: String
) : BaseUserBadge
