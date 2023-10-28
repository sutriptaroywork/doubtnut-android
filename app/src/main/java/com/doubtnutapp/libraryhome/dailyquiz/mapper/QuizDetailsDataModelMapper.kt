package com.doubtnutapp.libraryhome.dailyquiz.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.quizLibrary.entities.QuizDetailsEntity
import com.doubtnutapp.libraryhome.dailyquiz.model.QuizDetailsDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizDetailsDataModelMapper @Inject constructor() :
    Mapper<QuizDetailsEntity, QuizDetailsDataModel> {

    override fun map(quizDetailsEntity: QuizDetailsEntity) = with(quizDetailsEntity) {
        QuizDetailsDataModel(
            quizDetailsEntity.testId,
            quizDetailsEntity.classCode,
            quizDetailsEntity.subjectCode,
            quizDetailsEntity.chapterCode,
            quizDetailsEntity.title,
            quizDetailsEntity.description,
            quizDetailsEntity.durationInMin,
            quizDetailsEntity.solutionPdf,
            quizDetailsEntity.imageUrl,
            quizDetailsEntity.totalQuestions,
            quizDetailsEntity.publishTime,
            quizDetailsEntity.unpublishTime,
            quizDetailsEntity.isActive,
            quizDetailsEntity.difficultyType,
            quizDetailsEntity.type,
            quizDetailsEntity.ruleId,
            quizDetailsEntity.isSectioned,
            quizDetailsEntity.isDeleted,
            quizDetailsEntity.createdOn,
            quizDetailsEntity.canAttempt,
            quizDetailsEntity.canAttemptPromptMessage,
            quizDetailsEntity.testSubscriptionId,
            quizDetailsEntity.inProgress,
            quizDetailsEntity.attemptCount,
            quizDetailsEntity.lastGrade,
            getSubscriptionData(quizDetailsEntity.subscriptionData),
            quizDetailsEntity.bottomWidgetEntity
        )
    }

    private fun getSubscriptionData(subscriptionData: List<QuizDetailsEntity.QuizSubscriptionDataEntity>?): List<QuizDetailsDataModel.QuizSubscriptionData>? =
        subscriptionData?.map {
            getSubscriptionDataList(it)
        }

    private fun getSubscriptionDataList(testSubscriptionData: QuizDetailsEntity.QuizSubscriptionDataEntity): QuizDetailsDataModel.QuizSubscriptionData =
        testSubscriptionData.run {
            QuizDetailsDataModel.QuizSubscriptionData(
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