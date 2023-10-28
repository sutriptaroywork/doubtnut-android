package com.doubtnutapp.libraryhome.mocktest.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.libraryhome.mocktest.model.MockTestDetailsDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTestDetailsMapper @Inject constructor() : Mapper<MockTestDetailsDataModel, TestDetails> {

    override fun map(MockTestDetailsDataModel: MockTestDetailsDataModel) =
        with(MockTestDetailsDataModel) {
            TestDetails(
                testId,
                classCode,
                subjectCode,
                chapterCode,
                title,
                description,
                durationInMin,
                solutionPdf,
                totalQuestions,
                publishTime,
                unpublishTime,
                isActive,
                difficultyType,
                type,
                ruleId,
                isSectioned,
                isDeleted,
                createdOn,
                canAttempt,
                canAttemptPromptMessage,
                testSubscriptionId,
                inProgress,
                attemptCount,
                lastGrade,
                getSubscriptionData(subscriptionData) as ArrayList<TestDetails.TestSubscriptionData>?,
                bottomWidgetEntity
            )
        }

    fun getSubscriptionData(subscriptionData: List<MockTestDetailsDataModel.QuizSubscriptionData>?): List<TestDetails.TestSubscriptionData>? =
        subscriptionData?.map {
            getSubscriptionDataList(it)
        }

    private fun getSubscriptionDataList(testSubscriptionData: MockTestDetailsDataModel.QuizSubscriptionData): TestDetails.TestSubscriptionData =
        testSubscriptionData.run {
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

}