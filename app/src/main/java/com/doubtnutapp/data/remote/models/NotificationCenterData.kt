package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NotificationCenter(@SerializedName("notification") val notifications: List<NotificationCenterData>)

@Keep
data class NotificationCenterData(
    @SerializedName("event") val event: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("firebase_eventtag") val firebaseEvent: String?,
    @SerializedName("seen") var isClicked: Int?,
    @SerializedName("image") val imageUrl: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("sentAt") val sentAt: String?,
    @SerializedName("data") val data: HashMap<String, String>?,
    @SerializedName("id") val id: String?
)
