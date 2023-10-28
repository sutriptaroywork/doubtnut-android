package com.doubtnutapp.domain.quizLibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.mockTestLibrary.entities.BottomWidgetEntity

@Keep
data class QuizDetailsEntity(
    val testId: Int,
    val classCode: String? = null,
    val subjectCode: String? = null,
    val chapterCode: String? = null,
    val title: String? = null,
    val description: String? = null,
    val durationInMin: Int? = null,
    val solutionPdf: String? = null,
    val imageUrl: String? = null,
    val totalQuestions: Int? = null,
    val publishTime: String? = null,
    val unpublishTime: String? = null,
    val isActive: Int? = null,
    val difficultyType: String? = null,
    val type: String? = null,
    val ruleId: Int? = null,
    val isSectioned: Int? = null,
    val isDeleted: Int? = null,
    val createdOn: String? = null,
    val canAttempt: Boolean? = null,
    val canAttemptPromptMessage: String? = null,
    val testSubscriptionId: String? = null,
    val inProgress: Boolean? = null,
    val attemptCount: Int? = null,
    val lastGrade: String? = null,
    val subscriptionData: List<QuizSubscriptionDataEntity>? = null,
    val bottomWidgetEntity: BottomWidgetEntity?
) {

    @Keep
    data class QuizSubscriptionDataEntity(
        val id: Int,
        val testId: Int,
        val studentId: Int,
        val subjectCode: String?,
        val chapterCode: String?,
        val subtopicCode: String?,
        val mcCode: String?,
        val status: String?,
        val registeredAt: String?,
        val completedAt: String?
    )
}
