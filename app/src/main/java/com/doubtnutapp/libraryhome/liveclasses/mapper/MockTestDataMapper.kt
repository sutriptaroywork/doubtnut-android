package com.doubtnutapp.libraryhome.liveclasses.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestDetailsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTestDataMapper @Inject constructor() : Mapper<MockTestDetailsEntity, TestDetails> {

    override fun map(srcObject: MockTestDetailsEntity): TestDetails =
        TestDetails(
            srcObject.testId,
            srcObject.classCode,
            srcObject.subjectCode,
            srcObject.chapterCode,
            srcObject.title,
            srcObject.description,
            srcObject.durationInMin,
            srcObject.solutionPdf,
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
            ArrayList(getSubscriptionData(srcObject.subscriptionData)),
            srcObject.bottomWidgetEntity,
            srcObject.waitTime
        )

    private fun getSubscriptionData(list: List<MockTestDetailsEntity.TestSubscriptionDataEntity>?):
            List<TestDetails.TestSubscriptionData> {
        return list?.map {
            getSubscription(it)
        } ?: mutableListOf()
    }

    private fun getSubscription(testSubscriptionData: MockTestDetailsEntity.TestSubscriptionDataEntity): TestDetails.TestSubscriptionData =
        TestDetails.TestSubscriptionData(
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