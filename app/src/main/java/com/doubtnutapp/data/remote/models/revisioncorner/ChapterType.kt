package com.doubtnutapp.data.remote.models.revisioncorner

sealed class ChapterType {
    data class BottomSheetChapter(val chapterName: String) : ChapterType()
    data class RandomChapter(val chapterName: String) : ChapterType()
    data class RecentChapter(val chapterName: String) : ChapterType()
}
