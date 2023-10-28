package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize class QuestionMeta(
    val question_id: String,
    val ocr_text: String?,
    val question: String,
    val id: String?,
    @SerializedName("class") val clazz: String?,
    val chapter: String?,
    val subtopic: String?,
    @SerializedName("chapter_display") val chapterDisplay: String?,
    @SerializedName("subtopic_display") val subtopicDisplay: String?,
    val microconcept: String?,
    val secondary_microconcept: String?,
    val mc_text: String?,
    val secondary_mc_text: String?,
    val secondary_class: String?,
    val level: String?,
    val target_course: String?,
    var upvote_count: Int? = null,
    val student_username: String?,
    val is_answered: Int?,
    val image_url: String,
    val student_id: String?,
    var is_upvoted: Int?,
    val question_image: String?,
    val packages: String?,
    val timestamp: String?,
    val skip_message: String?,
    val question_thumbnail: String?,
    @SerializedName("doubt") val doubtField: String?,
    @SerializedName("show_share") val showShare: Int?,
    @SerializedName("package") val packaze: String?
) : Parcelable
