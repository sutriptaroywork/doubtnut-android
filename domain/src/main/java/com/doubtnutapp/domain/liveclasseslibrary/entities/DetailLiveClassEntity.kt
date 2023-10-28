package com.doubtnutapp.domain.liveclasseslibrary.entities

import androidx.annotation.Keep

@Keep
data class DetailLiveClassEntity(
    val pdfList: List<DetailLiveClassPdfEntity>,
    val liveClassList: List<LiveClassResourceEntity>,
    val videoList: List<LiveClassesCourseItem>,
    val chapterName: String,
    val videoCount: String,
    val liveStatus: Int,
    val timer: String
)
