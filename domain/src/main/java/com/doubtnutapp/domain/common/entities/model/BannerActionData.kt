package com.doubtnutapp.domain.common.entities.model

import androidx.annotation.Keep

@Keep
data class BannerActionData(
    val page: String,
    val qid: String,
    val playListId: String,
    val playlistTitle: String,
    val chapterId: String?,
    val exerciseName: String?,
    val id: String,
    val type: String,
    val studentClass: String,
    val course: String,
    val chapter: String,
    val externalUrl: String,
    val contestId: String,
    val pdfPackage: String,
    val levelOne: String,
    val tagKey: String,
    val tagValue: String
)
