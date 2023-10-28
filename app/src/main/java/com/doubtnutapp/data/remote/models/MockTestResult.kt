package com.doubtnutapp.data.remote.models

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.domain.mockTestLibrary.entities.BottomWidgetEntity
import com.google.gson.annotations.SerializedName

/**
 * Created by akshaynandwana on
 * 17, December, 2018
 **/
data class MockTestResult(
    @SerializedName("questionwise_result") val questionwiseResult: ArrayList<QuestionwiseResult>,
    @SerializedName("report_card") val reportCard: ReportCard,
    @SerializedName("sections") val sectionData: ArrayList<MockTestSectionData>,
    @SerializedName("bottom_widget") val bottomWidgetEntity: BottomWidgetEntity?,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("config_data") val configData: MockTestConfigData?,
    @SerializedName("show_result") val showResult: Boolean?,
    @SerializedName("attempted") val attempted: String?,
    @SerializedName("unattempted") val unattempted: String?,
    @SerializedName("index_data") val indexData: List<IndexData>?
) {

    data class ReportCard(
        @SerializedName("student_id") val studentId: Int,
        @SerializedName("test_id") val testId: Int,
        @SerializedName("test_subscription_id") val testSubscriptionId: Int,
        @SerializedName("totalscore") val totalScore: Int,
        @SerializedName("totalmarks") val totalMarks: Int,
        @SerializedName("correct") val correct: String?,
        @SerializedName("incorrect") val incorrect: String?,
        @SerializedName("skipped") val skipped: String?,
        @SerializedName("hide_result") val isResultHidden: Boolean?,
        @SerializedName("eligiblescore") val eligibleScore: Int
    )
    data class IndexData(
        @SerializedName("color") val color: String?,
        @SerializedName("text") val text: String?,
    )
}
