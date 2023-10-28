package com.doubtnutapp.domain.globalsearch.entities

import androidx.annotation.Keep

@Keep
data class GlobalSearchResultEntity(
    val resultType: String,
    val questionId: String?,
    val title: String?,
    val resultCourse: String?,
    val resultClass: String?,
    val chapter: String?,
    val microConceptId: String?,
    val subtopic: String?,
    val page: String?
)
