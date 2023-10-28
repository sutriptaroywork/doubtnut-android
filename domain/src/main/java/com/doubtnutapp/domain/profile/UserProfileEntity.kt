package com.doubtnutapp.domain.profile

data class UserProfileEntity(
    val studentId: String,
    val studentEmail: String?,
    val studentLocale: String?,
    val studentImageUrl: String?,
    val schoolName: String?,
    val studentClass: String,
    val studentTotalVideoViewDuration: String?,
    val studentUserName: String?,
    val StudentPinCode: String?,
    val studentCoaching: String?,
    val studentDOB: String?,
    val studentCourse: String?,
    val studentClassDisplay: String?,
    val studentLanguage: String?,
    val studentLanguageDisplay: String?
)
