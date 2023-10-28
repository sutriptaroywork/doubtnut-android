package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.doubtnutapp.domain.mockTestLibrary.entities.BottomWidgetEntity
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by akshaynandwana on
 * 17, December, 2018
 **/
@Parcelize
data class TestDetails(
    @SerializedName("test_id") val testId: Int,
    @SerializedName("class_code") val classCode: String? = null,
    @SerializedName("subject_code") val subjectCode: String? = null,
    @SerializedName("chapter_code") val chapterCode: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("duration_in_min") val durationInMin: Int? = null,
    @SerializedName("solution_pdf") val solutionPdf: String? = null,
    @SerializedName("no_of_questions") val totalQuestions: Int? = null,
    @SerializedName("publish_time") val publishTime: String? = null,
    @SerializedName("unpublish_time") val unpublishTime: String? = null,
    @SerializedName("is_active") val isActive: Int? = null,
    @SerializedName("difficulty_type") val difficultyType: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("rule_id") val ruleId: Int? = null,
    @SerializedName("is_sectioned") val isSectioned: Int? = null,
    @SerializedName("is_deleted") val isDeleted: Int? = null,
    @SerializedName("created_on") val createdOn: String? = null,
    @SerializedName("can_attempt") val canAttempt: Boolean? = null,
    @SerializedName("can_attempt_prompt_message") val canAttemptPromptMessage: String? = null,
    @SerializedName("test_subscription_id") val testSubscriptionId: String? = null,
    @SerializedName("in_progress") val inProgress: Boolean? = null,
    @SerializedName("attempt_count") val attemptCount: Int? = null,
    @SerializedName("last_grade") val lastGrade: String? = null,
    @SerializedName("subscriptionData") val subscriptionData: ArrayList<TestSubscriptionData>? = null,
    @SerializedName("bottom_widget") val bottomWidgetEntity: BottomWidgetEntity?,
    @SerializedName("wait_time") val testWaitTimeMillis: Long? = null,
) : Parcelable {

    @Parcelize
    data class TestSubscriptionData(
        @SerializedName("id") val id: Int,
        @SerializedName("test_id") val testId: Int,
        @SerializedName("student_id") val studentId: Int,
        @SerializedName("subject_code") val subjectCode: String?,
        @SerializedName("chapter_code") val chapterCode: String?,
        @SerializedName("subtopic_code") val subtopicCode: String?,
        @SerializedName("mc_code") val mcCode: String?,
        @SerializedName("status") val status: String?,
        @SerializedName("registered_at") val registeredAt: String?,
        @SerializedName("completed_at") val completedAt: String?
    ) : Parcelable
}
