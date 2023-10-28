package com.doubtnutapp.home.model

import androidx.annotation.Keep

@Keep
data class PhotoFeedViewItem(
        val id: String?,
        val type: String,
        val title: String?,
        val imageUrl: String?,
        val scrollSize: String?,
        val ratio: String,
        val sharingMessage: String?,
        val categoryTitle: String,
        override val viewType: Int
) : HomeFeedViewItem {
    companion object {
        const val type: String = "photo"
    }
}