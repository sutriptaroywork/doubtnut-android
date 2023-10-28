package com.doubtnutapp.gamification.gamepoints.model

import androidx.annotation.Keep

@Keep
data class ViewLevelInfoItemDataModel(val isAchieved: Boolean = false,
                                      val lvl: Int = 0,
                                      val xp: Int = 0,
                                      val isLast: Boolean = false)