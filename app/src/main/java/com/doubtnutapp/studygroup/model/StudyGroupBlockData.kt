package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity

@Keep
data class StudyGroupBlockData(
    val roomId: String,
    val studentId: String,
    val studentName: String,
    val confirmationPopup: ConfirmationPopup?,
    val adapterPosition: Int = -1,
    val members: List<String>,
    val actionSource: StudyGroupActivity.ActionSource? = null,
    val actionType: StudyGroupActivity.ActionType? = null
)
