package com.doubtnutapp.data.gamification.leaderboard.repository

import com.doubtnutapp.data.gamification.leaderboard.mapper.GameLeaderEntityMapper
import com.doubtnutapp.data.gamification.leaderboard.service.GameLeaderBoardService
import com.doubtnutapp.domain.gamification.leaderboard.entity.LeaderboardEntity
import com.doubtnutapp.domain.gamification.leaderboard.repository.GameLeaderBoardRepository
import io.reactivex.Single
import javax.inject.Inject

class GameLeaderBoardRepositoryImpl @Inject constructor(
    private val gameLeaderBoardService: GameLeaderBoardService,
    private val gameLeaderEntityMapper: GameLeaderEntityMapper
) : GameLeaderBoardRepository {

    override fun getGameLeaders(): Single<LeaderboardEntity> =
        gameLeaderBoardService.getGameLeaders().map {
            gameLeaderEntityMapper.map(it.data)
        }
}
