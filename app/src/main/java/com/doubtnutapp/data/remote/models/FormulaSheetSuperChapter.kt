package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class FormulaSheetSuperChapter(
    @SerializedName("id") val superChapterId: String,
    @SerializedName("name") val superChapterName: String,
    @SerializedName("subject_id") val superChapterSubjectId: String,
    @SerializedName("chapters") val chapterList: ArrayList<FormulaSheetChapter>,
    @SerializedName("icon_path") val superChapterImagePath: String?
) {
    data class FormulaSheetChapter(
        @SerializedName("id") val chapterId: String,
        @SerializedName("name") val chapterName: String,
        @SerializedName("subject_id") val chapterSubjectId: String,
        @SerializedName("super_chapter_id") val chapterSuperChapterId: String,
        @SerializedName("icon_path") val chapterImagePath: String?

    )
}
