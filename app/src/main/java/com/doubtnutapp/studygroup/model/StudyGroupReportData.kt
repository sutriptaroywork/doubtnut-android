package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep

@Keep
data class StudyGroupReportData(
    val roomId: String,
    val reportType: String,
    val reportMessage: ReportMessage?,
    val reportMember: ReportMember?,
    val reportGroup: ReportGroup?,
    val reportReasons: ReportReasons,
    val isAdmin: Boolean
)

@Keep
data class ReportMessage(
    val messageId: String?,
    val senderId: String?,
    val millis: Long?,
)

@Keep
data class ReportMember(
    val reportedStudentId: String,
    val reportedStudentName: String,
)

@Keep
data class ReportGroup(
    val adminId: String
)
