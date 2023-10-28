package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AcceptStudyGroupInvitation (
        @SerializedName("message") val message: String,
        @SerializedName("is_member_joined") val isMemberJoined: Boolean,
        @SerializedName("is_already_member") val isAlreadyMember: Boolean?,
        @SerializedName("is_previously_blocked") val isPreviouslyBlocked: Boolean?,
        @SerializedName("description") val description: String?,
        @SerializedName("socket_msg") val socketMsg: String?,
        @SerializedName("cta_text") val ctaText: String?
)