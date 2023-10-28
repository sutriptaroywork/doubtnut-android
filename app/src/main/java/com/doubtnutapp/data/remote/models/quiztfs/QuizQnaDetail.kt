package com.doubtnutapp.data.remote.models.quiztfs

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuizQnaDetail(
    @SerializedName("questionID") val questionID: String,
    @SerializedName("questionText") val questionText: String,
    @SerializedName("optionA") val optionA: String,
    @SerializedName("optionB") val optionB: String,
    @SerializedName("optionC") val optionC: String,
    @SerializedName("optionD") val optionD: String,
    @SerializedName("answerSelected") val answerSelected: List<String>?,
    @SerializedName("actualAnswer") val actualAnswer: List<String>?,
    @SerializedName("ptsReceived") val ptsReceived: String?
) : Parcelable

@Parcelize
data class QuizTfsData(
    @SerializedName("type") val type: String,
    @SerializedName("questionDetails") val questionDetails: QuizQnaDetail,
    @SerializedName("streakText") val streakText: String?,
    @SerializedName("streakColor") val streakColor: String?,
    @SerializedName("questionTypeTitle") val questionTypeTitle: String,
    @SerializedName("point") val point: String,
    @SerializedName("progressColor") val progressColor: String,
    @SerializedName("progressBgColor") val progressBgColor: String,
    @SerializedName("studentClass") val studentClass: String,
    @SerializedName("language") val language: String,
    @SerializedName("subject") val subject: String,
) : Parcelable

@Parcelize
data class QuizQnaInfoApi(
    @SerializedName("timeToNext") val timeToNext: Long,
    @SerializedName("timeToRespond") val timeToRespond: Long,
    @SerializedName("timeToWait") val timeToWait: Long?,
    @SerializedName("waitData") val waitData: QuizTfsWaitData?,
    @SerializedName("data") val data: QuizTfsData?,
    @SerializedName("sessionId") val sessionId: String?,
    @SerializedName("pageTitle") val pageTitle: String?
) : Parcelable

data class QuizTfsSubmitResponse(
    @SerializedName("correctAnswers") val correctAnswers: List<String>?,
    @SerializedName("totalPoints") val totalPoints: String
)
