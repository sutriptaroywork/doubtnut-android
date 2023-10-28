package com.doubtnutapp.data.common.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiBannerActionData(
    @SerializedName("page") val page: String,
    @SerializedName("qid") val qid: String,
    @SerializedName("playlist_id") val playListId: String,
    @SerializedName("playlist_title") val playlistTitle: String,
    @SerializedName("chapter_id") val chapterId: String,
    @SerializedName("exercise_name") val exerciseName: String,
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("class") val studentClass: String,
    @SerializedName("course") val course: String,
    @SerializedName("chapter") val chapter: String,
    @SerializedName("url") val externalUrl: String,
    @SerializedName("contest_id") val contestId: String,
    @SerializedName("pdf_package") val pdfPackage: String,
    @SerializedName("level_one") val levelOne: String,
    @SerializedName("tag_key") val tagKey: String,
    @SerializedName("is_last") val isLast: Int?,
    @SerializedName("tag_value") val tagValue: String,
    @SerializedName("faculty_id") val facultyId: Int?,
    @SerializedName("ecm_id") val ecmId: Int?,
    @SerializedName("subject") val subject: String
)
