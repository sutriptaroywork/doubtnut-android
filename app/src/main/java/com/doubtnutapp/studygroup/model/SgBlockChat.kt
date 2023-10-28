package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SgBlockChat(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("own_blocked_status") val ownBlockedStatus: Int,
    @SerializedName("other_blocked_status") val otherBlockedStatus: Int,
)