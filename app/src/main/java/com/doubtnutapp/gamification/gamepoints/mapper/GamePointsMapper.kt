package com.doubtnutapp.gamification.gamepoints.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.gamePoints.entity.ActionConfigDataItemEntity
import com.doubtnutapp.domain.gamification.gamePoints.entity.GamePointsEntity
import com.doubtnutapp.domain.gamification.gamePoints.entity.ViewLevelInfoItemEntity
import com.doubtnutapp.gamification.gamepoints.model.ActionConfigDataItemDataModel
import com.doubtnutapp.gamification.gamepoints.model.GamePointsDataModel
import com.doubtnutapp.gamification.gamepoints.model.ViewLevelInfoItemDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamePointsDataModelMapper @Inject constructor() : Mapper<GamePointsEntity, GamePointsDataModel> {

    override fun map(srcObject: GamePointsEntity) = with(srcObject) {
        GamePointsDataModel(
            currentLvlImg,
            currentLvlPoints,
            heading,
            currentLvl,
            getActionConfigDataList(actionConfigData),
            nextCurrentImg,
            title,
            dailyPoint,
            points,
            getViewLevelInfoList(viewLevelInfo),
            historyText,
            nextLvl,
            nextLvlPoints,
            nextLevelPercentage
        )
    }

    private fun getActionConfigDataList(apiActionConfigDataItem: List<ActionConfigDataItemEntity>?): List<ActionConfigDataItemDataModel>? {
        return apiActionConfigDataItem?.map {
            ActionConfigDataItemDataModel(
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

    private fun getViewLevelInfoList(gameGamePoints: List<ViewLevelInfoItemEntity>?): List<ViewLevelInfoItemDataModel>? {
        return gameGamePoints?.mapIndexed { index, value ->
            ViewLevelInfoItemDataModel(
                value.isAchieved == 1,
                value.lvl,
                value.xp,
                index == gameGamePoints.lastIndex
            )
        }
    }
}