package com.doubtnutapp.libraryhome.liveclasses.model

import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class VideoViewItem(
    val type: String,
    val title: String,
    val status: Int,
    val qId: String,
    val youtubeId: String,
    val imageUrl: String,
    val duration: String,
    val liveAt: String,
    val dayText: String,
    val timeText: String,
    val resourceType: String,
    val topicList: List<String>,
    val pageData: HashMap<String, String>?,
    val event: String?,
    override val viewType: Int
) : LiveClassesFeedViewItem()