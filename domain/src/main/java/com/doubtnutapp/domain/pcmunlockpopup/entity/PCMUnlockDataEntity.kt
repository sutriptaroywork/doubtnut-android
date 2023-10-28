package com.doubtnutapp.domain.pcmunlockpopup.entity

import androidx.annotation.Keep

@Keep
data class PCMUnlockDataEntity(
    val heading: String,
    val subHeading: String,
    val requiredBadges: List<BadgeRequired>,
    val leadingUsersImageUrl: List<String>,
    val footerText: String
)
