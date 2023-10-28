package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by akshaynandwana on
 * 09, October, 2018
 **/
@Parcelize
data class QuizReport(
    @SerializedName("question_id") val questionId: Int,
    @SerializedName("quiz_id") val quizId: Int,
    @SerializedName("q_text_en") val qTextEn: String,
    @SerializedName("q_type") val qType: Int,
    @SerializedName("q_image") val qImage: String,
    @SerializedName("q_video") val qVideo: String,
    @SerializedName("q_pos_mark") val qPosMark: Int,
    @SerializedName("q_neg_mark") val qNegMark: Int,
    @SerializedName("is_active") val isActive: Int,
    @SerializedName("is_attempt") val isAttempt: Int,
    @SerializedName("is_correct") val isCorrect: Int,
    @SerializedName("is_eligible") val isEligible: Int,
    @SerializedName("option_value") val optionValue: ArrayList<QuizReportOptions>
) : Parcelable {

    @Parcelize
    data class QuizReportOptions(
        @SerializedName("question_id") val questionId: Int,
        @SerializedName("quiz_id") val quizId: Int,
        @SerializedName("option_value") val optionValue: String,
        @SerializedName("option_id") val optionId: Int,
        @SerializedName("is_correct") val isCorrect: Int,
        @SerializedName("is_user_selected") val isUserSelected: Int
    ) : Parcelable
}
