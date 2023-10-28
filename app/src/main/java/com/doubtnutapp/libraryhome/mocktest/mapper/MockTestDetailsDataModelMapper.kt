package com.doubtnutapp.libraryhome.mocktest.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestDetailsEntity
import com.doubtnutapp.libraryhome.mocktest.model.MockTestDetailsDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTestDetailsDataModelMapper @Inject constructor() :
    Mapper<MockTestDetailsEntity, MockTestDetailsDataModel> {

    override fun map(mockTestDetailsEntity: MockTestDetailsEntity) = with(mockTestDetailsEntity) {
        MockTestDetailsDataModel(
            mockTestDetailsEntity.testId,
            mockTestDetailsEntity.classCode,
            mockTestDetailsEntity.subjectCode,
            mockTestDetailsEntity.chapterCode,
            mockTestDetailsEntity.title,
            mockTestDetailsEntity.description,
            mockTestDetailsEntity.durationInMin,
            mockTestDetailsEntity.solutionPdf,
            mockTestDetailsEntity.imageUrl,
            mockTestDetailsEntity.totalQuestions,
            mockTestDetailsEntity.publishTime,
            mockTestDetailsEntity.unpublishTime,
            mockTestDetailsEntity.isActive,
            mockTestDetailsEntity.difficultyType,
            mockTestDetailsEntity.type,
            mockTestDetailsEntity.ruleId,
            mockTestDetailsEntity.isSectioned,
            mockTestDetailsEntity.isDeleted,
            mockTestDetailsEntity.createdOn,
            mockTestDetailsEntity.canAttempt,
            mockTestDetailsEntity.canAttemptPromptMessage,
            mockTestDetailsEntity.testSubscriptionId,
            mockTestDetailsEntity.inProgress,
            mockTestDetailsEntity.attemptCount,
            mockTestDetailsEntity.lastGrade,
            getSubscriptionData(mockTestDetailsEntity.subscriptionData),
            bottomWidgetEntity
        )
    }

    private fun getSubscriptionData(subscriptionData: List<MockTestDetailsEntity.TestSubscriptionDataEntity>?): List<MockTestDetailsDataModel.QuizSubscriptionData>? =
        subscriptionData?.map {
            getSubscriptionDataList(it)
        }

    private fun getSubscriptionDataList(testSubscriptionData: MockTestDetailsEntity.TestSubscriptionDataEntity): MockTestDetailsDataModel.QuizSubscriptionData =
        testSubscriptionData.run {
            MockTestDetailsDataModel.QuizSubscriptionData(
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