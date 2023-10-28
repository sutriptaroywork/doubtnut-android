package com.doubtnutapp.gamification.badgesscreen.model

import androidx.annotation.Keep

@Keep
data class BadgeHeaderViewType(
        val title: String,
        override val viewType: Int
): BaseBadgeViewType