package com.doubtnutapp.domain.pcmunlockpopup.entity

import androidx.annotation.Keep

@Keep
data class BadgeRequired(
    val currentProgress: Int,
    val description: String,
    val id: Int,
    val imageUrl: String,
    val name: String,
    val nudgeDescription: String,
    val requirement: Int,
    val requirementType: String
)
