package com.doubtnutapp.data.gamification.gamePoints.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.gamePoints.model.ApiActionConfigDataItem
import com.doubtnutapp.data.gamification.gamePoints.model.ApiGamePoints
import com.doubtnutapp.data.gamification.gamePoints.model.ApiViewLevelInfoItem
import com.doubtnutapp.domain.gamification.gamePoints.entity.ActionConfigDataItemEntity
import com.doubtnutapp.domain.gamification.gamePoints.entity.GamePointsEntity
import com.doubtnutapp.domain.gamification.gamePoints.entity.ViewLevelInfoItemEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamePointsEntityMapper @Inject constructor() : Mapper<ApiGamePoints, GamePointsEntity> {

    override fun map(srcObject: ApiGamePoints) = with(srcObject) {
        GamePointsEntity(
            currentLvlImg,
            currentLvlPoints,
            heading,
            currentLvl,
            getActionConfigDataList(actionConfigData),
            nextCurrentImg,
            title,
            dailyPoint,
            allpoints,
            getViewLevelInfoList(viewLevelInfo),
            historyText,
            nextLvl,
            nextLvlPoints,
            nextLevelPercentage
        )
    }

    private fun getActionConfigDataList(apiActionConfigDataItem: List<ApiActionConfigDataItem>?): List<ActionConfigDataItemEntity>? {
        return apiActionConfigDataItem?.map {
            ActionConfigDataItemEntity(
                it.isActive,
                it.xp,
                it.action,
                it.createdAt,
                it.id,
                it.actionDisplay,
                it.actionPage
            )
        }
    }

    private fun getViewLevelInfoList(gameGamePoints: List<ApiViewLevelInfoItem>?): List<ViewLevelInfoItemEntity>? {
        return gameGamePoints?.map {
            ViewLevelInfoItemEntity(
                it.isAchieved,
                it.level,
                it.xp
            )
        }
    }
}
