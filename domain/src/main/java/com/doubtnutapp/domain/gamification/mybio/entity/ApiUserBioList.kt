package com.doubtnutapp.domain.gamification.mybio.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiUserBioList(
    @SerializedName("active") val isActive: String?,
    @SerializedName("options") val options: List<ApiUserBioListOption>
)

@Keep
data class ApiUserBioListOption(
    @SerializedName("id") val id: Int?,
    @SerializedName("alias") val alias: String?,
    @SerializedName("name") val className: String?,
    @SerializedName("selected") val selected: Int?,
    @SerializedName("img_url") val imageUrl: String?,
    @SerializedName("custom") val custom: String?,
    @SerializedName("stream_list") val streamList: ArrayList<Stream>?
)

@Keep
data class Stream(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("selected") val selected: Int
)
