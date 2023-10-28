package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class InvitedToStudyGroup (
        @SerializedName("is_invited") val isInvited: Boolean,
        @SerializedName("message") val message: String?
)