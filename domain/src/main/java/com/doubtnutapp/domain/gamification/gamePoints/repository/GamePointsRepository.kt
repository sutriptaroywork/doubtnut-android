package com.doubtnutapp.domain.gamification.gamePoints.repository

import com.doubtnutapp.domain.gamification.gamePoints.entity.GamePointsEntity
import io.reactivex.Single

interface GamePointsRepository {

    fun getMilestoneAndAction(): Single<GamePointsEntity>
}
