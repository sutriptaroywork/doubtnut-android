package com.doubtnutapp.domain.gamification.leaderboard.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.leaderboard.entity.LeaderboardEntity
import com.doubtnutapp.domain.gamification.leaderboard.repository.GameLeaderBoardRepository
import io.reactivex.Single
import javax.inject.Inject

class GetGameLeaders @Inject constructor(
    private val gameLeaderBoardRepository: GameLeaderBoardRepository
) : SingleUseCase<LeaderboardEntity, Unit> {

    override fun execute(param: Unit): Single<LeaderboardEntity> =
        gameLeaderBoardRepository.getGameLeaders()
}
