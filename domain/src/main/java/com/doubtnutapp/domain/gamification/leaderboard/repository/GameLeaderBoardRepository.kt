package com.doubtnutapp.domain.gamification.leaderboard.repository

import com.doubtnutapp.domain.gamification.leaderboard.entity.LeaderboardEntity
import io.reactivex.Single

interface GameLeaderBoardRepository {

    fun getGameLeaders(): Single<LeaderboardEntity>
}
