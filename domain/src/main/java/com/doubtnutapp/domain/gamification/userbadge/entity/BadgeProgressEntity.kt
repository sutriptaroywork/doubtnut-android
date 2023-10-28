package com.doubtnutapp.domain.gamification.userbadge.entity

import androidx.annotation.Keep

@Keep
data class BadgeProgressEntity(
    val requirements: List<RequirementsItemEntity>?,
    val nudgeDescription: String = "",
    val imageUrl: String = "",
    val name: String = "",
    val description: String = ""
)
