package com.doubtnutapp.domain.gamification.mybio.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiUserBoardList(
    @SerializedName("active") val isActive: String?,
    @SerializedName("options") val options: HashMap<String, List<ApiUserBioListOption>>
)
