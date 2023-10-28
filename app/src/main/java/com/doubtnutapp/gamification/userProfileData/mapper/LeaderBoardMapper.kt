package com.doubtnutapp.gamification.userProfileData.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.userProfile.entity.DailyLeaderboardItemEntity
import com.doubtnutapp.gamification.userProfileData.model.DailyLeaderboardItemDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderBoardMapper @Inject constructor() : Mapper<DailyLeaderboardItemEntity, DailyLeaderboardItemDataModel> {
    override fun map(srcObject: DailyLeaderboardItemEntity) = with(srcObject) {
        DailyLeaderboardItemDataModel(
                leaderBoardProfileImage,
                leaderBoardUserId,
                leaderBoardUserName,
                leaderBoardRank,
                leaderBoardPoints

        )
    }

}