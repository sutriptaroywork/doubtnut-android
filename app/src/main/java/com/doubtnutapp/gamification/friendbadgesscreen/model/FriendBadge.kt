package com.doubtnutapp.gamification.friendbadgesscreen.model

import androidx.annotation.Keep

@Keep
data class FriendBadge(val badgeIconUrl: String,
                       val isAchieved: Boolean,
                       val name: String)