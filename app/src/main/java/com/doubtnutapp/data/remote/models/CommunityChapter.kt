package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class CommunityChapter(
    val chapter: String,
    @SerializedName("chapter_display")val chapterDisplay: String
)
