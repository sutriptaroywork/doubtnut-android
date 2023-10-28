package com.doubtnutapp.domain.gamification.userbadge.repository

import com.doubtnutapp.domain.gamification.userbadge.entity.BadgeProgressEntity
import com.doubtnutapp.domain.gamification.userbadge.entity.BaseUserBadge
import io.reactivex.Single

interface UserBadgesRepository {
    fun getUserBadgesList(userId: String): Single<List<BaseUserBadge>>

    fun getUserBadgeProgress(badgeId: String): Single<BadgeProgressEntity>
}
