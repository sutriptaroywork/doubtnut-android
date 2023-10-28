package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep

@Keep
data class StudyGroupDeleteData(
    val widgetType: String?,
    val isAdmin: Boolean,
    val deleteType: String,
    val roomId: String,
    val deleteMessageData: DeleteMessageData?,
    val deleteReportedMessages: DeleteReportedMessages?,
    val confirmationPopup: ConfirmationPopup?,
    val adapterPosition: Int = -1
)

@Keep
data class DeleteMessageData(
    val messageId: String?,
    val millis: Long?,
    val senderId: String?
)

@Keep
data class DeleteReportedMessages(
    val roomId: String,
    val reportedStudentId: String,
)
