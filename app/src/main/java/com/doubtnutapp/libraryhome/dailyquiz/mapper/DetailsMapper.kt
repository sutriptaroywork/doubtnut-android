package com.doubtnutapp.libraryhome.dailyquiz.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.libraryhome.dailyquiz.model.QuizDetailsDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetailsMapper @Inject constructor() : Mapper<QuizDetailsDataModel, TestDetails> {

    override fun map(quizDetailsDataModel : QuizDetailsDataModel) = with(quizDetailsDataModel) {
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



    fun getSubscriptionData(subscriptionData: List<QuizDetailsDataModel.QuizSubscriptionData>?): List<TestDetails.TestSubscriptionData>? = subscriptionData?.map {
        getSubscriptionDataList(it)
    }

    private fun getSubscriptionDataList(testSubscriptionData: QuizDetailsDataModel.QuizSubscriptionData): TestDetails.TestSubscriptionData = testSubscriptionData.run {
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