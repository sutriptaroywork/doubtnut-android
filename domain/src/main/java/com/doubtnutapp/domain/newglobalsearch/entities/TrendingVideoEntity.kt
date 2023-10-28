package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class TrendingVideoEntity(
    val id: Int,
    val questionId: Int,
    val `class`: Int,
    val subject: String,
    val chapter: String,
    val doubt: String,
    val ocrText: String,
    val question: String,
    val bgColor: String,
    val type: String,
    val isActive: Int,
    val imageUrl: String,
    val deeplinkUrl: String?
) : SearchSuggestionsFeedItem
