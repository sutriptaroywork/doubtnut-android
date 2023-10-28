package com.doubtnutapp.data.mocktestLibrary.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.mocktestLibrary.model.ApiMockTest
import com.doubtnutapp.data.mocktestLibrary.model.ApiMockTestDetails
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestDetailsEntity
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTestEntityMapper @Inject constructor() : Mapper<ApiMockTest, MockTestEntity> {

    override fun map(srcObject: ApiMockTest) = with(srcObject) {
        MockTestEntity(
            course,
            getMockTestDetails(mockTestList)

        )
    }

    private fun getMockTestDetails(mockTestList: List<ApiMockTestDetails>): List<MockTestDetailsEntity> = mockTestList.map {
        getMockTestDetailsData(it)
    }

    private fun getMockTestDetailsData(apiMockTestDetails: ApiMockTestDetails): MockTestDetailsEntity = apiMockTestDetails.run {
        MockTestDetailsEntity(
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
            apiMockTestDetails.bottomWidgetEntity,
            apiMockTestDetails.waitTime
        )
    }

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
