package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
sealed class SgSocketResponse

@Keep
data class BlockStudyGroupMember (
        @SerializedName("student_id") val studentId: String?,
) : SgSocketResponse()

@Keep
data class SgReport (
        @SerializedName("message") val message: String?,
        @SerializedName("student_id") val studentId: String?,
) : SgSocketResponse()

@Keep
data class SgDelete (
        @SerializedName("message") val message: String?,
        @SerializedName("student_id") val studentId: String?,
) : SgSocketResponse()