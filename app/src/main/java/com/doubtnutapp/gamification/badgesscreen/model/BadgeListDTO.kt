package com.doubtnutapp.gamification.badgesscreen.model

import androidx.annotation.Keep

@Keep
data class BadgeListDTO(
        val badgeListName: String,
        val badgeListData: List<Badge>
)