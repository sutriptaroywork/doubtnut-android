package com.doubtnutapp.domain.gamification.userbadge.entity

import androidx.annotation.Keep

@Keep
data class RequirementsItemEntity(
    val requirementType: String = "",
    val fullfilled: Int = 0,
    val fullfilledPercent: Int = 0,
    val requirement: Int = 0
)
