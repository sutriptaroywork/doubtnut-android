package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 20/08/21.
 */

@Keep
@Parcelize
data class ShortTestSubmitData(
    @SerializedName("all_questions") val allQuestions: List<String>,
    @SerializedName("correct_questions") val correctQuestions: List<String>,
    @SerializedName("incorrect_questions") val incorrectQuestions: List<String>,
    @SerializedName("widget_id") val widgetId: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("chapter_alias") val chapterAlias: String,
    @SerializedName("submitted_options") val submittedOptions: List<SubmittedOption>,
) : Parcelable

@Keep
@Parcelize
data class SubmittedOption(
    @SerializedName("question_id") val questionId: String,
    @SerializedName("selected_option") val selectedOption: String,
) : Parcelable
