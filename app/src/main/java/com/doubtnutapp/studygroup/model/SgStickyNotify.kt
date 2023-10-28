package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SgStickyNotify(
    @SerializedName("is_sticky_available") val isStickyAvailable: Boolean?,
    @SerializedName("is_report_available") val isReportAvailable: Boolean?,
    @SerializedName("title") val title: String?,
    @SerializedName("deeplink") val deeplink: String?,
)
