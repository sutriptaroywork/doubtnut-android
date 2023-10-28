package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SgSetting(
        @SerializedName("notification_container") val notificationContainer: NotificationContainer,
        @SerializedName("block_list_container") val blockListContainer: BlockListContainer,
        @SerializedName("title") val title: String,
)

@Keep
data class NotificationContainer(
        @SerializedName("title") val title: String,
        @SerializedName("toggle") val toggle: Boolean,
)

@Keep
data class BlockListContainer(
        @SerializedName("title") val title: String,
        @SerializedName("count") val count: String,
        @SerializedName("deeplink") val deeplink: String,
)

@Keep
data class subAdminRequestData(
        @SerializedName("message") val message: String?,
)

