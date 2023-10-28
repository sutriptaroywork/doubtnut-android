package com.doubtnutapp.data.structuredCourse.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.mocktestLibrary.model.ApiMockTestDetails
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestDetailsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTestMapper @Inject constructor() : Mapper<ApiMockTestDetails, MockTestDetailsEntity> {

    override fun map(srcObject: ApiMockTestDetails): MockTestDetailsEntity =
        MockTestDetailsEntity(
            srcObject.testId,
            srcObject.classCode,
            srcObject.subjectCode,
            srcObject.chapterCode,
            srcObject.title,
            srcObject.description,
            srcObject.durationInMin,
            srcObject.solutionPdf,
            srcObject.imageUrl,
            srcObject.totalQuestions,
            srcObject.publishTime,
            srcObject.unpublishTime,
            srcObject.isActive,
            srcObject.difficultyType,
            srcObject.type,
            srcObject.ruleId,
            srcObject.isSectioned,
            srcObject.isDeleted,
            srcObject.createdOn,
            srcObject.canAttempt,
            srcObject.canAttemptPromptMessage,
            srcObject.testSubscriptionId,
            srcObject.inProgress,
            srcObject.attemptCount,
            srcObject.lastGrade,
            getSubscriptionData(srcObject.subscriptionData),
            srcObject.bottomWidgetEntity,
            srcObject.waitTime
        )

    fun getSubscriptionData(subscriptionData: List<ApiMockTestDetails.TestSubscriptionData>?): List<MockTestDetailsEntity.TestSubscriptionDataEntity>? = subscriptionData?.map {
        getSubscriptionDataList(it)
    }

    private fun getSubscriptionDataList(testSubscriptionData: ApiMockTestDetails.TestSubscriptionData): MockTestDetailsEntity.TestSubscriptionDataEntity = testSubscriptionData.run {
        MockTestDetailsEntity.TestSubscriptionDataEntity(
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
