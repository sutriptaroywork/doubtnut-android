package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep

@Keep
data class StudyGroupRemoveContainerData(
    val roomId: String,
    val containerId: String,
    val containerType: String,
    val adapterPosition: Int,
)