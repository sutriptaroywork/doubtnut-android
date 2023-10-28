package com.doubtnutapp.liveclass.ui.practice_english

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Akshat Jindal on 18/12/21.
 */

data class PracticeEnglishData(
    @SerializedName("title") val title: String?,
    @SerializedName("questions_list") val question: List<Question>?,
    @SerializedName("session_id") val sessionId: String?
)

data class Question(
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("display_type") val question_type: String?
)

enum class QuestionType {
    AUDIO_QUESTION,
    TEXT_QUESTION,
    IMAGE_QUESTION,
    SINGLE_CHOICE_QUESTION,
    SINGLE_BLANK_QUESTION,
    MULTI_BLANK_QUESTION,
    INVALID;

    companion object {
        fun getQuestionType(type: String): QuestionType {
            for (questionType in values())
                if (questionType.name == type)
                    return questionType
            return INVALID
        }
    }
}

data class AnswerData(
    @SerializedName("correct") val correct: Boolean?,
    @SerializedName("matchPercent") val matchPercent: String?,
    @SerializedName("correctText") val correctText: String?,
    @SerializedName("percentageTextColor") val percentageTextColor: String?,
    @SerializedName("yourAnswerText") val yourAnswerText: String?,
    @SerializedName("userTextDisplay") val userTextDisplay: String?,
    @SerializedName("userAudioUrl") val userAudioUrl: String?,
    @SerializedName("correctAnswerText") val correctAnswerText: String?,
    @SerializedName("correctTextDisplay") val correctTextDisplay: String?,
    @SerializedName("answerAudioUrl") val answerAudioUrl: String?,
    @SerializedName("try_again_button_text") val tryAgainButtonText: String?,
    @SerializedName("try_again_upload_url") val tryAgainUploadUrl: String?,
    @SerializedName("next_button_text") val nextButtonText: String?,
    @SerializedName("options") val options: List<MCQQuestionDataItem>?
)

data class MCQQuestionDataItem(
    @SerializedName("isChosen") var isSelected: Boolean?,
    @SerializedName("isCorrect") var isCorrect: Boolean?,
    @SerializedName("option") val text: String?,
)

open class QuestionData

@Parcelize
data class AudioQuestionData(
    @SerializedName("title") val title: String?,
    @SerializedName("question_audio") val question_audio: String?,
    @SerializedName("question") val question: String?,
    @SerializedName("submit_button_text") val submit_text: String?,
    @SerializedName("answer_audio_upload_url") val answerAudioUploadUrl: String?
) : QuestionData(), Parcelable

@Parcelize
data class TextQuestionData(
    @SerializedName("title") val title: String?,
    @SerializedName("question") val question: String?,
    @SerializedName("question_audio") val question_audio: String?,
    @SerializedName("write_ans_hint") val write_ans_hint: String?,
    @SerializedName("show_mic") val show_mic: Boolean?,
    @SerializedName("submit_button_text") val submit_text: String?,
    @SerializedName("language")val language: String?
) : QuestionData(), Parcelable

@Parcelize
data class ImageQuestionData(
    @SerializedName("title") val title: String,
    @SerializedName("question") val question: String?,
    @SerializedName("question_image_url") val question_image_url: String?,
    @SerializedName("submit_button_text") val submit_text: String?,
    @SerializedName("otherOptionText") val otherOptionText: String?,
    @SerializedName("options") val options: List<String>?
) : QuestionData(), Parcelable

@Parcelize
data class SingleChoiceQuestionData(
    @SerializedName("title") val title: String?,
    @SerializedName("question") val question: String?,
    @SerializedName("question_audio") val question_audio: String?,
    @SerializedName("submit_button_text") val submit_text: String?,
    @SerializedName("otherOptionText") val otherOptionText: String?,
    @SerializedName("options") val options: List<String>?
) : QuestionData(), Parcelable

@Parcelize
data class SingleBlankQuestionData(
    @SerializedName("title") val title: String?,
    @SerializedName("question") val question: List<String>?,
    @SerializedName("answer_audio") val answer_audio: String?,
    @SerializedName("submit_button_text") val submit_text: String?,
    @SerializedName("answer_text") val answer_text: String?,
) : QuestionData(), Parcelable

@Parcelize
data class MultiBlankQuestionData(
    @SerializedName("title") val title: String?,
    @SerializedName("question") val question: List<String>?,
    @SerializedName("otherOptions") val otherOptions: List<String>?,
    @SerializedName("question_audio") val question_audio: String?,
    @SerializedName("answer_audio") val answer_audio: String?,
    @SerializedName("answer_text") val answer_text: String?,
    @SerializedName("submit_button_text") val submit_text: String?,
    @SerializedName("refresh_button_text") val refresh_text: String?,
    @SerializedName("otherOptionText") val otherOptionText: String?,
) : QuestionData(), Parcelable

@Parcelize
data class PracticeEndScreenData(
    @SerializedName("image_url") val image_url: String?,
    @SerializedName("display_title_text") val display_title_text: String?,
    @SerializedName("display_subtitle_text") val display_subtitle_text: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("practice_more_button_text") val practice_more_button_text: String?,
    @SerializedName("try_again_button_text") val try_again_button_text: String?,
    @SerializedName("reminder_text") val reminder_text: String?,
    @SerializedName("questions") val questions: List<PracticeEnglishWidget.PracticeEnglishWidgetItems>?,
    @SerializedName("session_id") val sessionId: String?,
) : Parcelable