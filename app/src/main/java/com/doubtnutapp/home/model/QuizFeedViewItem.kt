package com.doubtnutapp.home.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.mockTestLibrary.entities.BottomWidgetEntity

@Keep
data class QuizFeedViewItem(
    val id: String?,
    val type: String?,
    val title: String?,
    val description: String?,
    val buttonBgColor: String?,
    val buttonText: String?,
    val imageUrl: String?,
    val buttonTextColor: String?,
    val unpublishTime: String?,
    val testSubscriptionId: String?,
    val attemptCount: Int?,
    val publishTime: String?,
    val testId: Int,
    val classCode: String?,
    val subjectCode: String?,
    val chapterCode: String?,
    val durationInMin: Int?,
    val solutionPdf: String?,
    val totalQuestions: Int?,
    val isActive: Int?,
    val ruleId: Int?,
    val canAttempt: Boolean?,
    val numberOfQuestions: Int?,
    val scrollSize: String?,
    val ratio: String,
    val sharingMessage: String?,
    val categoryTitle: String,
    val bottomWidgetEntity: BottomWidgetEntity?,
    override val viewType: Int
) : HomeFeedViewItem {
    companion object {
        const val type: String = "quiz"
    }
}