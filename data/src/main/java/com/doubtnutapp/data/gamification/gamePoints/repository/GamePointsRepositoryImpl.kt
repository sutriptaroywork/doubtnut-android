package com.doubtnutapp.data.gamification.gamePoints.repository

import com.doubtnutapp.data.gamification.gamePoints.mapper.GamePointsEntityMapper
import com.doubtnutapp.data.gamification.gamePoints.service.GamePointsService
import com.doubtnutapp.domain.gamification.gamePoints.entity.GamePointsEntity
import com.doubtnutapp.domain.gamification.gamePoints.repository.GamePointsRepository
import io.reactivex.Single
import javax.inject.Inject

class GamePointsRepositoryImpl @Inject constructor(
    private val gamePointsService: GamePointsService,
    private val gamePointsEntityMapper: GamePointsEntityMapper
) : GamePointsRepository {

    override fun getMilestoneAndAction(): Single<GamePointsEntity> {
        return gamePointsService.getUserMilestonesAndGameActions().map {
            gamePointsEntityMapper.map(it.data)
        }
    }
}
