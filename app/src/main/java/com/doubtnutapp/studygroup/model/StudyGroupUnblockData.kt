package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep

@Keep
data class StudyGroupUnblockData(
    val chatId: String,
    val studentId: String,
    val confirmationPopup: ConfirmationPopup?,
    val adapterPosition: Int = -1,
    val type: String
)
