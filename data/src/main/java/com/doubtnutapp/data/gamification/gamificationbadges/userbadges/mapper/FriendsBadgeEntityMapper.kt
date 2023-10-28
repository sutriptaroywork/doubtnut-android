package com.doubtnutapp.data.gamification.gamificationbadges.userbadges.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.model.ApiUserBadge
import com.doubtnutapp.domain.gamification.friendsbadges.entity.FriendsBadgeEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendsBadgeEntityMapper @Inject constructor() : Mapper<ApiUserBadge, FriendsBadgeEntity> {
    override fun map(srcObject: ApiUserBadge) = with(srcObject) {
        FriendsBadgeEntity(
            imageUrl.orEmpty(),
            isAchieved,
            name.orEmpty()
        )
    }
}
