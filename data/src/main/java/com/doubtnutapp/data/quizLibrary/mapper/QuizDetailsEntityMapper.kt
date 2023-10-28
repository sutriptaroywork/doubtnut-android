package com.doubtnutapp.data.quizLibrary.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.quizLibrary.model.ApiQuizDetails
import com.doubtnutapp.domain.quizLibrary.entities.QuizDetailsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizDetailsEntityMapper @Inject constructor() : Mapper<ApiQuizDetails, QuizDetailsEntity> {

    override fun map(apiMockTestDetails: ApiQuizDetails) = with(apiMockTestDetails) {
        QuizDetailsEntity(
            apiMockTestDetails.testId,
            apiMockTestDetails.classCode,
            apiMockTestDetails.subjectCode,
            apiMockTestDetails.chapterCode,
            apiMockTestDetails.title,
            apiMockTestDetails.description,
            apiMockTestDetails.durationInMin,
            apiMockTestDetails.solutionPdf,
            apiMockTestDetails.imageUrl,
            apiMockTestDetails.totalQuestions,
            apiMockTestDetails.publishTime,
            apiMockTestDetails.unpublishTime,
            apiMockTestDetails.isActive,
            apiMockTestDetails.difficultyType,
            apiMockTestDetails.type,
            apiMockTestDetails.ruleId,
            apiMockTestDetails.isSectioned,
            apiMockTestDetails.isDeleted,
            apiMockTestDetails.createdOn,
            apiMockTestDetails.canAttempt,
            apiMockTestDetails.canAttemptPromptMessage,
            apiMockTestDetails.testSubscriptionId,
            apiMockTestDetails.inProgress,
            apiMockTestDetails.attemptCount,
            apiMockTestDetails.lastGrade,
            getSubscriptionData(apiMockTestDetails.subscriptionData),
            apiMockTestDetails.bottomWidgetEntity
        )
    }

    fun getSubscriptionData(subscriptionData: List<ApiQuizDetails.ApiQuizSubscriptionData>?): List<QuizDetailsEntity.QuizSubscriptionDataEntity>? =
        subscriptionData?.map {
            getSubscriptionDataList(it)
        }

    private fun getSubscriptionDataList(testSubscriptionData: ApiQuizDetails.ApiQuizSubscriptionData): QuizDetailsEntity.QuizSubscriptionDataEntity =
        testSubscriptionData.run {
            QuizDetailsEntity.QuizSubscriptionDataEntity(
                testSubscriptionData.id,
                testSubscriptionData.testId,
                testSubscriptionData.studentId,
                testSubscriptionData.subjectCode,
                testSubscriptionData.chapterCode,
                testSubscriptionData.subtopicCode,
                testSubscriptionData.mcCode,
                testSubscriptionData.status,
                testSubscriptionData.registeredAt,
                testSubscriptionData.completedAt

            )
        }
}
