package com.doubtnutapp.domain.videoPage.entities

import androidx.annotation.Keep

@Keep
data class MicroConceptEntity(
    val mcId: String?,
    val chapter: String?,
    val mcClass: Int?,
    val mcCourse: String?,
    val mcSubtopic: String?,
    val mcQuestionId: String?,
    val mcAnswerId: String?,
    val mcVideoDuration: String?,
    var mcText: String?,
    val mcVideoId: String?
)
