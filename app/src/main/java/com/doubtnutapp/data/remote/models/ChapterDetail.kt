package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class ChapterDetail(
    val stats: Stats,
    val subtopics: ArrayList<Subtopic>
) {

    data class Stats(
        val total_duration: Int,
        val total_videos: Int,
        val count_concept: Int,
        val chapter_display: String,
        val chapter: String,
        @SerializedName("path_image") val pathImage: String
    )
}
