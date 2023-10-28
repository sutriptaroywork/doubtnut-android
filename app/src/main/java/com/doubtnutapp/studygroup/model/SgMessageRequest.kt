package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SendMessageRequestData(
        @SerializedName("message") val message: String,
        @SerializedName("is_invited") val isInvited: Boolean,
        @SerializedName("deeplink") val deeplink: String,
)

@Keep
data class AcceptMessageRequestData(
        @SerializedName("message") val message: String,
        @SerializedName("is_member_joined") val isMemberJoined: Boolean,
        @SerializedName("description") val description: String?,
        @SerializedName("is_already_member") val isAlreadyMember: Boolean,
        @SerializedName("socket_msg") val socketMessage: String?,
)