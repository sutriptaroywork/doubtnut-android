package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by akshaynandwana on
 * 17, December, 2018
 **/
@Parcelize
data class TestResult(
    @SerializedName("questionwise_result") val questionwiseResult: ArrayList<QuestionwiseResult>,
    @SerializedName("report_card") val reportCard: ReportCard
) : Parcelable {

    @Parcelize
    data class ReportCard(
        @SerializedName("student_id") val studentId: Int,
        @SerializedName("test_id") val testId: Int,
        @SerializedName("test_subscription_id") val testSubscriptionId: Int,
        @SerializedName("totalscore") val totalScore: Int,
        @SerializedName("totalmarks") val totalMarks: Int,
        @SerializedName("correct") val correct: String?,
        @SerializedName("incorrect") val incorrect: String?,
        @SerializedName("skipped") val skipped: String?,
        @SerializedName("eligiblescore") val eligibleScore: Int
    ) : Parcelable
}
