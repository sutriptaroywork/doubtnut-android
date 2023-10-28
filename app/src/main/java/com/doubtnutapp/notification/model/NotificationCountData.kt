package com.doubtnutapp.notification.model

import androidx.annotation.Keep
import com.doubtnutapp.data.remote.models.NotificationCenterData
import com.google.gson.annotations.SerializedName

@Keep
data class NotificationCountData(@SerializedName("count") val count: String?)
