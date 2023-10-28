package com.doubtnutapp.domain.gamification.userbadge.entity

import androidx.annotation.Keep

@Keep
data class UserBadgeEntity(
    val id: Int,
    val name: String,
    val description: String,
    val nudgeDescription: String,
    val imageUrl: String,
    val isAchieved: Int,
    val sharingMessage: String,
    val actionPage: String,
    val blurImage: String,
    override val type: String
) : BaseUserBadge
