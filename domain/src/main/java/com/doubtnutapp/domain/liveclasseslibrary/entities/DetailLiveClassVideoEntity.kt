package com.doubtnutapp.domain.liveclasseslibrary.entities

import androidx.annotation.Keep
import com.google.gson.JsonObject

@Keep
data class DetailLiveClassVideoEntity(
    val type: String,
    val title: String,
    val status: Int,
    val qId: String,
    val youtubeId: String?,
    val imageUrl: String,
    val duration: String,
    val liveAt: String,
    val dayText: String,
    val timeText: String,
    val resourceType: String,
    val topicList: List<String>,
    val pageData: JsonObject?,
    val event: String?
) : LiveClassesCourseItem {
    companion object {
        const val type = "lecture"
    }
}
