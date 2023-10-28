package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.doubtnutapp.data.remote.models.revisioncorner.RulesInfo
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.base.RecyclerViewItem
import com.google.gson.annotations.SerializedName

@Keep
data class HomeWorkQuestionData(
    @SerializedName("header") val header: HomeWorkHeader?,
    @SerializedName("list", alternate = ["questions"]) val questionList: List<HomeWorkQuestion>?,
    @SerializedName("button") val button: ButtonData?,
    @SerializedName("pdf_download_url") var pdfDownloadUrl: String?,
    @SerializedName("share_message") var shareMessage: String?,
    @SerializedName("rules_info") val rulesInfo: RulesInfo?,
    @SerializedName("completion_status") val completionStatus: String?,
    @SerializedName("completion_image_url") val completionImageUrl: String?,
)

@Keep
data class HomeWorkHeader(
    @SerializedName("title1") val lectureName: String?,
    @SerializedName("title2") val teacherName: String?,
    @SerializedName("question_count_text") val questionCount: String?
)

@Keep
data class HomeWorkQuestion(
    @SerializedName("question") val question: String?,
    @SerializedName("quiz_question_id") val quizQuestionId: String?,
    @SerializedName("solution_question_id") val solutionQuestionId: String?,
    @SerializedName("page") val page: String?,
    @SerializedName("question_type") var type: String?,
    @SerializedName("type") var questionType: String?,
    @SerializedName("solution_deeplink") var solutionDeeplink: String?,
    @SerializedName("solution_text") var solutionText: String?,
    @SerializedName("question_no_text") var questionNumberText: String?,
    @SerializedName("options") val options: List<HomeWorkOption>?,
    @SerializedName("answer") val answer: String?,
    @SerializedName("submitted_option") val submittedOption: String?,
    @SerializedName("is_result") val isResult: Boolean,
    @SerializedName("video_text") val videoText: String?,
    @SerializedName("video_deeplink") val videoDeeplink: String?,
    @SerializedName("widget") val widget: WidgetEntityModel<*, *>?,
    override val viewType: Int
): RecyclerViewItem {
    fun isCorrect(submittedOptionIndex: Int): Boolean {
        if (options.isNullOrEmpty()) return false
        return options[submittedOptionIndex].key == answer
    }
}

@Keep
data class HomeWorkOption(
    @SerializedName("key") val key: String?,
    @SerializedName("value") val value: String?,
    var isSelected: Boolean? = false
)

@Keep
data class ButtonData(
    @SerializedName("title") val buttonText: String?,
    @SerializedName("next_text") val nextText: String?,
    @SerializedName("previous_text") val previousText: String?,
)

@Keep
data class HomeWorkSolutionData(
    @SerializedName("header") val header: HomeWorkHeader?,
    @SerializedName("summary") val summary: List<HomeWorkSolutionSummary>?,
    @SerializedName("detailed_summary") val detailedSummary: List<HomeWorkSolutionDetailedSummary>?,
    @SerializedName("solutions") val solutionList: List<HomeWorkVideoSolution>?,
    @SerializedName("pdf_download_url") var pdfDownloadUrl: String?,
    @SerializedName("share_message") var shareMessage: String?,
    @SerializedName("solutions_playlist_id") val solutionsPlaylistId: String?,
    @SerializedName("question_ids") val questionIds: List<String>?,
    @SerializedName("progress_report_icon") val progressReportIcon: String?,
    @SerializedName("questions", alternate = ["list"]) val questions: List<HomeWorkQuestion>?,
    @SerializedName("completion_status") val completionStatus: String?,
    @SerializedName("completion_status_url") val completionStatusUrl: String?,
)

@Keep
data class HomeWorkSolutionSummary(
    @SerializedName("text") val text: String?,
    @SerializedName("count") val count: String?,
    @SerializedName("color") val color: String?
)

@Keep
data class HomeWorkSolutionDetailedSummary(
    @SerializedName("question_no") val questionNo: String?,
    @SerializedName("correct") val isCorrect: Boolean?,
    @SerializedName("incorrect") val isIncorrect: Boolean?,
    @SerializedName("skipped") val isSkipped: Boolean?,
    @SerializedName("color") val color: String?
)

@Keep
data class HomeWorkVideoSolution(
    @SerializedName("title") val title: String?,
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("ocr_text") val ocrText: String?,
    @SerializedName("locale") val locale: String?,
    @SerializedName("asked") val asked: String?,
    @SerializedName("deeplink") val deeplink: String?
)

@Keep
data class HomeWorkPostData(
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("response") val responseList: List<HomeWorkResponse>
)

@Keep
data class HomeWorkResponse(
    @SerializedName("quiz_question_id") val questionId: String?,
    @SerializedName("option_id") val optionId: String?
)

@Keep
data class HomeWorkResponseData(
    @SerializedName("message") val message: String?
)

@Keep
data class HomeWorkListResponse(
    @SerializedName("title") val title: String?,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>
)
