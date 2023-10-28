package com.doubtnutapp.data.remote.models.topicboostergame2

sealed class ChapterType {
    data class BottomSheetChapter(val chapterName: String) : ChapterType()
    data class RandomChapter(val chapterName: String) : ChapterType()
    data class RecentChapter(val chapterName: String) : ChapterType()
}
