package com.doubtnutapp.gamification.badgesscreen.model

import androidx.annotation.Keep

@Keep
data class BadgeProgress(val requirements: List<RequirementsItemDataModel>?,
                         val nudgeDescription: String = "",
                         val imageUrl: String = "",
                         val name: String = "",
                         val description: String = "")