package com.doubtnutapp.data.gamification.gamePoints.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiGamePoints(

    @SerializedName("current_lvl_img") val currentLvlImg: String = "",
    @SerializedName("current_lvl_points") val currentLvlPoints: Int = 0,
    @SerializedName("heading") val heading: String = "",
    @SerializedName("current_lvl") val currentLvl: String = "",
    @SerializedName("action_config_data") val actionConfigData: List<ApiActionConfigDataItem>?,
    @SerializedName("next_current_img") val nextCurrentImg: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("daily_point") val dailyPoint: Int = 0,
    @SerializedName("points") val allpoints: Int = 0,
    @SerializedName("view_level_info") val viewLevelInfo: List<ApiViewLevelInfoItem>?,
    @SerializedName("history_text") val historyText: String = "",
    @SerializedName("next_lvl") val nextLvl: String = "",
    @SerializedName("next_lvl_points") val nextLvlPoints: Int = 0,
    @SerializedName("next_level_percentage") val nextLevelPercentage: Int = 0

)
