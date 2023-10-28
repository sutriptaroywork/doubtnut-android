package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep

@Keep
data class TrendingPdfAndBooksViewItem(
        val name: String,
        val id: Int,
        val description: String,
        val imageUrl: String,
        val isLast: Int,
        val type: String,
        val resourceType: String,
        val resourcePath: String,
        val `class`: Int,
        val subject: String,
        val isActive: Int,
        override val viewType: Int
) : TrendingSearchFeedViewItem