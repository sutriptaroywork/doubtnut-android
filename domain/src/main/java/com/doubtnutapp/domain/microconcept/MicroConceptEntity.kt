package com.doubtnutapp.domain.microconcept

import androidx.annotation.Keep

@Keep
data class MicroConceptEntity(
    var microConceptId: String,
    val chapter: String,
    val clazz: Int,
    val course: String,
    val subtopic: String?,
    var microConceptText: String,
    val id: String?
)
