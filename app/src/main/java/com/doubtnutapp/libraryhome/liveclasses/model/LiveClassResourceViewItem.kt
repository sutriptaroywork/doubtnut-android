package com.doubtnutapp.libraryhome.liveclasses.model

import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class LiveClassResourceViewItem(
    val type: String,
    val title: String,
    val qId: String,
    val imageUrl: String,
    val liveAt: String,
    val topicList: List<String>,
    val page: String?,
    override val viewType: Int
) : LiveClassesFeedViewItem()