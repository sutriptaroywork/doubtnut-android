package com.doubtnut.core.widgets.entities

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class ActionData(
    @SerializedName("pdf_url", alternate = ["pdfUrl"]) val pdfUrl: String? = null,
    @SerializedName("pdf_package", alternate = ["pdfPackage"]) val pdfPackage: String? = null,
    @SerializedName("page") val page: String? = null,
    @SerializedName("qid") val qid: String? = null,
    @SerializedName("playlist_id", alternate = ["playListId"]) val playListId: String? = null,
    @SerializedName(
        "playlist_title",
        alternate = ["playlistTitle"]
    ) val playlistTitle: String? = null,
    @SerializedName("is_last", alternate = ["isLast"]) val isLast: Int? = null,
    @SerializedName("chapter_id", alternate = ["chapterId"]) val chapterId: String? = null,
    @SerializedName("exercise_name", alternate = ["exerciseName"]) val exerciseName: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("class", alternate = ["studentClass"]) val studentClass: String? = null,
    @SerializedName("course") val course: String? = null,
    @SerializedName("chapter") val chapter: String? = null,
    @SerializedName("url", alternate = ["externalUrl"]) val externalUrl: String? = null,
    @SerializedName("contest_id", alternate = ["contestId"]) val contestId: String? = null,
    @SerializedName("level_one", alternate = ["levelOne"]) val levelOne: String? = null,
    @SerializedName("tag_key", alternate = ["tagKey"]) val tagKey: String? = null,
    @SerializedName("tag_value", alternate = ["tagValue"]) val tagValue: String? = null,
    @SerializedName("faculty_id", alternate = ["facultyId"]) val facultyId: Int? = null,
    @SerializedName("ecm_id", alternate = ["ecmId"]) val ecmId: Int? = null,
    @SerializedName("subject") val subject: String? = null,
    @SerializedName("lecture_id", alternate = ["lectureId"]) val lectureId: Int? = null,
    @SerializedName("category_id", alternate = ["categoryId"]) val categoryId: String? = null,
    @SerializedName("page_type", alternate = ["pageType"]) val pageType: String? = null,
    @SerializedName("course_type", alternate = ["courseType"]) val courseType: String? = null,
    @SerializedName(
        "payment_deeplink",
        alternate = ["paymentDeeplink"]
    ) val paymentDeeplink: String? = null,
    @SerializedName("assortment_id", alternate = ["assortmentId"]) val assortmentId: String? = null
) : Parcelable