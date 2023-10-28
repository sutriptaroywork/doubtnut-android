package com.doubtnutapp.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class InAppPopupResponse(
    val deeplink: String?,
    @SerializedName("notification_popup_data")
    val notificationPopupData: AppEvent?
)