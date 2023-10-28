package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep

@Keep
data class TrendingVideoViewItem(
        val id: Int,
        val questionId: String,
        val `class`: Int,
        val subject: String,
        val chapter: String,
        val doubt: String,
        val ocrText: String,
        val question: String,
        val bgColor: String,
        val type: String,
        val isActive: Int,
        val imageUrl : String,
        val deeplinkUrl : String?,
        val eventType : String?,
        override val viewType: Int
) : TrendingSearchFeedViewItem