package com.doubtnutapp.gamification.mybio.model

import androidx.annotation.Keep

@Keep
data class UserBoardListDataModel (
        val isActive: String,
        val options: HashMap<String, List<UserBioListOptionDataModel>>
)

