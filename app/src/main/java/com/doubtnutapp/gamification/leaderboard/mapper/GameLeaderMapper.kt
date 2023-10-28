package com.doubtnutapp.gamification.leaderboard.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.leaderboard.entity.GameLeaderEntity
import com.doubtnutapp.domain.gamification.leaderboard.entity.LeaderboardEntity
import com.doubtnutapp.gamification.leaderboard.model.GameLeader
import com.doubtnutapp.gamification.leaderboard.model.LeaderboardData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameLeaderMapper @Inject constructor() : Mapper<LeaderboardEntity, LeaderboardData> {

    override fun map(srcObject: LeaderboardEntity): LeaderboardData =
        LeaderboardData(
            allLeaderboardData = getLeaderboardData(srcObject.allLeaderboardData),
            dailyLeaderboardData = getLeaderboardData (srcObject.dailyLeaderboardData)
        )

    private fun getLeaderboardData(dataList: List<GameLeaderEntity>): List<GameLeader> =
        dataList.map {
            getGameLeader(it)
        }

    private fun getGameLeader(gameLeaderEntity: GameLeaderEntity): GameLeader =
        with(gameLeaderEntity) {
            GameLeader(
                profileImage,
                rank,
                userId,
                userName,
                points,
                isOwn
            )
        }
}