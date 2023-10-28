package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LeaveStudyGroup (
        @SerializedName("message") val message: String?,
        @SerializedName("socket_msg") val socketMsg: String?,
        @SerializedName("is_group_left") val isGroupLeft: Boolean?
)