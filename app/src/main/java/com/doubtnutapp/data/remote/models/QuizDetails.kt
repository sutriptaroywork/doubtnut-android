package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by akshaynandwana on
 * 26, September, 2018
 **/
@Parcelize
data class QuizDetails(
    @SerializedName("quiz_id") val quizId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("time_start") val timeStart: String,
    @SerializedName("time_end") val timeEnd: String,
    @SerializedName("subject") val subject: String,
    @SerializedName("class") val _class: Int,
    @SerializedName("description") val description: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_active") val isActive: Int,
    @SerializedName("is_attempt") val isAttempt: Int,
    @SerializedName("questions") val questions: ArrayList<QuizDetailsQuestions>,
    @SerializedName("rules") val rules: ArrayList<QuizRules>
) : Parcelable {

    @Parcelize
    data class QuizDetailsQuestions(
        @SerializedName("question_id") val questionId: Int,
        @SerializedName("quiz_id") val quizId: Int,
        @SerializedName("q_text_en") val qTextEn: String,
        @SerializedName("q_type") val qType: Int,
        @SerializedName("q_image") val qImage: String?,
        @SerializedName("q_video") val qVideo: String?,
        @SerializedName("q_pos_mark") val qPosMark: Int,
        @SerializedName("q_neg_mark") val qNegMark: Int,
        @SerializedName("is_active") val isActive: Int,
        @SerializedName("option_value") val optionValue: ArrayList<QuizDetailsOptionValue>
    ) : Parcelable {

        @Parcelize
        data class QuizDetailsOptionValue(
            @SerializedName("question_id") val questionId: Int,
            @SerializedName("quiz_id") val quizId: Int,
            @SerializedName("option_value") val optionValue: String,
            @SerializedName("option_id") val optionId: Int
        ) : Parcelable
    }

    @Parcelize
    data class QuizRules(
        @SerializedName("quiz_id") val quizId: Int,
        @SerializedName("rule_id") val ruleId: Int,
        @SerializedName("rule") val rule: String
    ) : Parcelable
}
