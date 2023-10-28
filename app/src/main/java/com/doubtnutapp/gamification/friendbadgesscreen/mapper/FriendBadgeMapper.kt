package com.doubtnutapp.gamification.friendbadgesscreen.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.friendsbadges.entity.FriendsBadgeEntity
import com.doubtnutapp.gamification.friendbadgesscreen.model.FriendBadge
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendBadgeMapper @Inject constructor() : Mapper<FriendsBadgeEntity, FriendBadge> {

    override fun map(srcObject: FriendsBadgeEntity) = with(srcObject) {
        FriendBadge(
                imageUrl,
                isAchieved == ACHIEVED,
                name
        )
    }

    companion object {
        private const val ACHIEVED = 1
    }
}