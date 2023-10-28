package com.doubtnutapp.gamification.mybio.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.gamification.mybio.entity.Stream

@Keep
data class UserBioListDataModel(
    val isActive: String,
    val options: List<UserBioListOptionDataModel>
)

@Keep
data class UserBioListOptionDataModel(
    val id: Int,
    val alias: String,
    val className: String,
    val selected: Int,
    val imageUrl: String,
    val custom: String,
    val streamList: ArrayList<Stream>?
)

