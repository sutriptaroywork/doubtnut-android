package com.doubtnutapp.gamification.gamepoints.model

import androidx.annotation.Keep

@Keep
data class GamePointsDataModel(val currentLvlImg: String = "",
                               val currentLvlPoints: Int = 0,
                               val heading: String = "",
                               val currentLvl: String = "",
                               val actionConfigData: List<ActionConfigDataItemDataModel>?,
                               val nextCurrentImg: String = "",
                               val title: String = "",
                               val dailyPoint: Int = 0,
                               val points: Int = 0,
                               val viewLevelInfo: List<ViewLevelInfoItemDataModel>?,
                               val historyText: String = "",
                               val nextLvl: String = "",
                               val nextLvlPoints: Int = 0,
                               val nextLevelPercentage: Int = 0
)