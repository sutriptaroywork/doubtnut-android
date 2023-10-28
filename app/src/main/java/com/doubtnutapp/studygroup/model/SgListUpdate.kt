package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SgListUpdate(
    @SerializedName("data") val data: List<SgUpdatedList>,
)

@Keep
data class SgUpdatedList(
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("message") val message: String?,
    @SerializedName("sender_name") val senderName: String?,
    @SerializedName("unread_count") val unreadCount: Int,
    @SerializedName("room_id") val roomId: String,
    @SerializedName("last_sent_time") val lastSentTime: Long,
)