package com.doubtnutapp.domain.common.entities.model

import com.google.gson.annotations.SerializedName

data class ApiPopUp(
    @SerializedName("type") val type: String?,
    @SerializedName("should_show") val shouldShow: Boolean?,
    @SerializedName("rating_data") val subData: ApiPopUpSubData?
)
