package com.doubtnutapp.gamification.badgesscreen.model

import androidx.annotation.Keep

@Keep
data class RequirementsItemDataModel(val requirementType: String = "",
                                     val fullfilled: Int = 0,
                                     val fullfilledPercent: Int = 0,
                                     val requirement: Int = 0)