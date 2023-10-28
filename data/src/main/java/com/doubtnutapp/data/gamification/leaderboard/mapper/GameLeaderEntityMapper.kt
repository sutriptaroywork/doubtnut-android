package com.doubtnutapp.data.gamification.leaderboard.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.leaderboard.model.ApiGameLeader
import com.doubtnutapp.data.gamification.leaderboard.model.ApiLeaderboardData
import com.doubtnutapp.domain.gamification.leaderboard.entity.GameLeaderEntity
import com.doubtnutapp.domain.gamification.leaderboard.entity.LeaderboardEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameLeaderEntityMapper @Inject constructor() : Mapper<ApiLeaderboardData, LeaderboardEntity> {

    override fun map(srcObject: ApiLeaderboardData): LeaderboardEntity =
        LeaderboardEntity(
            allLeaderboardData = getLeaderboardData(srcObject.allLeaderboardData),
            dailyLeaderboardData = getLeaderboardData(srcObject.dailyLeaderboardData)
        )

    private fun getLeaderboardData(dataList: List<ApiGameLeader>): List<GameLeaderEntity> =
        dataList.map {
            getGameLeaderData(it)
        }

    private fun getGameLeaderData(srcObject: ApiGameLeader): GameLeaderEntity =
        GameLeaderEntity(
            srcObject.profileImage,
            srcObject.rank,
            srcObject.userId,
            srcObject.userName,
            srcObject.points,
            srcObject.isOwn == 1
        )
}
