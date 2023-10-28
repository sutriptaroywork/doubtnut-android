package com.doubtnutapp.domain.common.entities.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiPopUpSubData(
    @SerializedName("header") val header: String?,
    @SerializedName("subheader") val subHeader: String?,
    @SerializedName("option") val options: List<String>?,
    @SerializedName("show_google_review") val showGoogleReview: Boolean?
)
