package com.doubtnutapp.domain.textsolution.entities

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
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
