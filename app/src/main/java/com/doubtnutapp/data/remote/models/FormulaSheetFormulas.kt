package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class FormulaSheetFormulas(
    @SerializedName("id") val formulasId: String,
    @SerializedName("name") val formulasName: String,
    @SerializedName("subject_id") val formulasSubjectId: String,
    @SerializedName("chapter_id") val formulasChapterId: String,

    @SerializedName("formulas") val formulasList: ArrayList<FormulasList>,
    @SerializedName("icon_path") val formulasImagePath: String?,
    @SerializedName("parent_topic_id") val formulasParentTopic: String?

) {
    data class FormulasList(
        @SerializedName("id") val formulaItemId: String,
        @SerializedName("name") val formulaItemName: String,
        @SerializedName("subject_id") val formulaItemSubjectId: String,
        @SerializedName("super_chapter_id") val formulaItemSuperChapterId: String,
        @SerializedName("chapter_id") val formulaItemChapterId: String,

        @SerializedName("image_url") val formulaItemImageUrl: String?,
        @SerializedName("html") val formulaItemHTML: String?,
        @SerializedName("max_image_height") val maxHeightImage: String?,
        @SerializedName("is_marked_for_memorize") val isMarkedForMemorize: String?,
        @SerializedName("topic_id") val formulaItemTopicId: String?,
        @SerializedName("webview_height") val WebViewMaxHeight: String?,
        @SerializedName("legends") val shortCutName: ArrayList<FormulasShortCutList>?,
        @SerializedName("constants") val constantsName: ArrayList<FormulasShortCutList>?
    ) {

        data class FormulasShortCutList(

            @SerializedName("id") val formulaItemShortCutId: String,
            @SerializedName("short_name") var formulaItemShortCutShortName: String,
            @SerializedName("full_name") val formulaItemShortCutFullName: String,
            @SerializedName("formula_id") val formulaItemShortCutFormulaId: String,
            @SerializedName("value") val formulaItemConstantsValue: String?
        )
    }
}
