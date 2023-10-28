package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FormulaSheetGlobalSearch(

    @SerializedName("search_type") val searchType: String,
    @SerializedName("formulas") val formulasList: List<FormulaSheetGlobalList>?,
    @SerializedName("chapters") val chapterList: List<FormulaSheetGlobalList>?,
    @SerializedName("topics") val topicList: List<FormulaSheetGlobalList>?

) : Parcelable {
    @Parcelize
    data class FormulaSheetGlobalList(
        @SerializedName("id") val globalSearchId: String?,
        @SerializedName("name") val globalSearchName: String?,
        @SerializedName("subject_id") val globalSearchSubjectId: String?,
        @SerializedName("chapter_id") val globalSearchChapterId: String?,
        @SerializedName("super_chapter_id") val globalSearchSuperChapterId: String?

    ) : Parcelable
}
