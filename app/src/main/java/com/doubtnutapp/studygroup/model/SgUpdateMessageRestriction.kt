package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SgUpdateMessageRestriction (
        @SerializedName("is_chat_enabled") val isChatEnabled: Boolean,
        @SerializedName("group_minimum_member_warning_message") val groupMinimumMemberWarningMessage: String?,
)