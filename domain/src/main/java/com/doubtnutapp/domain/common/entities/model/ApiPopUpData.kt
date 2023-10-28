package com.doubtnutapp.domain.common.entities.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiPopUpData(
    @SerializedName("pop_up_list") val popUpList: List<ApiPopUp>
)
