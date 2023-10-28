package com.doubtnutapp.data.gamification.gamificationbadges.userProfile.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.userProfile.model.ApiDailyLeaderboardItem
import com.doubtnutapp.domain.gamification.userProfile.entity.DailyLeaderboardItemEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyLeaderBoardEntityMapper @Inject constructor() : Mapper<ApiDailyLeaderboardItem, DailyLeaderboardItemEntity> {
    override fun map(srcObject: ApiDailyLeaderboardItem) = with(srcObject) {
        DailyLeaderboardItemEntity(
            leaderBoardProfileImage ?: "",
            leaderBoardUserId,
            leaderBoardUserName,
            leaderBoardRank,
            leaderBoardPoints.orEmpty()
        )
    }
}
