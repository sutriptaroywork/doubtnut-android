package com.doubtnutapp.data.gamification.gamificationbadges.userbadges.repository

import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.mapper.UserBadgeListEntityMapper
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.mapper.userBadgeProgressEntityMapper
import com.doubtnutapp.domain.gamification.userbadge.entity.BadgeProgressEntity
import com.doubtnutapp.domain.gamification.userbadge.entity.BaseUserBadge
import com.doubtnutapp.domain.gamification.userbadge.repository.UserBadgesRepository
import io.reactivex.Single
import javax.inject.Inject

class UserBadgeRepositoryImpl @Inject constructor(
    private val userBadgeService: UserBadgeService,
    private val userBadgeListEntityMapper: UserBadgeListEntityMapper,
    private val userBadgeProgressEntityMapper: userBadgeProgressEntityMapper,
    private val userPreference: UserPreference
) : UserBadgesRepository {

    override fun getUserBadgesList(userId: String): Single<List<BaseUserBadge>> {
        return userBadgeService.getBadges(userId).map {
            userBadgeListEntityMapper.map(it.data)
        }
    }

    override fun getUserBadgeProgress(badgeId: String): Single<BadgeProgressEntity> {
        return userBadgeService.getBadgeProgress(badgeId).map {
            userBadgeProgressEntityMapper.map(it.data)
        }
    }
}
