package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.doubtnutapp.domain.mockTestLibrary.entities.BottomWidgetEntity
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuestionwiseResult(
    @SerializedName("test_id") val testId: Int,
    @SerializedName("section_code") val sectionCode: String?,
    @SerializedName("questionbank_id") val questionbankId: String?,
    @SerializedName("difficulty_type") val difficultyType: String?,
    @SerializedName("correct_reward") val correctReward: Int,
    @SerializedName("incorrect_reward") val incorrectReward: Int,
    @SerializedName("validity") val validity: Int?,
    @SerializedName("is_active") val isActive: Int,
    @SerializedName("created_on") val createdOn: String,
    @SerializedName("id") val id: Int,
    @SerializedName("subject_code") val subjectCode: String?,
    @SerializedName("chapter_code") val chapterCode: String?,
    @SerializedName("subtopic_code") val subtopicCode: String?,
    @SerializedName("mc_code") val mcCode: String?,
    @SerializedName("class_code") val classCode: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("loc_lang") val locLang: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("video_url") val videoUrl: String?,
    @SerializedName("audio_url") val audioUrl: String?,
    @SerializedName("doubtnut_questionid") val doubtnutQuestionId: Int?,
    @SerializedName("options") val options: ArrayList<TestOptionsId>,
    @SerializedName("is_correct") val isCorrect: Int?,
    @SerializedName("is_skipped") val isSkipped: Int?,
    @SerializedName("marks_scored") val marksScored: Int?,
    @SerializedName("section_title") val sectionTitle: String?,
    @SerializedName("bottom_widget") val bottomWidgetEntity: BottomWidgetEntity?
) : Parcelable {

    @Parcelize
    data class TestOptionsId(
        @SerializedName("option_code") val optionCode: String?,
        @SerializedName("questionbank_id") val questionbankId: String?,
        @SerializedName("title") val title: String,
        @SerializedName("description") val description: String?,
        @SerializedName("is_answer") val isAnswer: String?,
        @SerializedName("answer") val userAnswer: String?,
        @SerializedName("loc_lang") val locLang: String?,
        @SerializedName("difficulty_type") val difficultyType: String?,
        @SerializedName("created_on") val createdOn: String,
        @SerializedName("is_selected") val isSelected: Int
    ) : Parcelable
}
