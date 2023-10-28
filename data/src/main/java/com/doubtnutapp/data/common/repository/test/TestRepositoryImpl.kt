package com.doubtnutapp.data.common.repository.test

import com.doubtnutapp.data.common.model.test.AttemptedTestModel
import com.doubtnutapp.domain.common.entities.model.AttemptedTest
import com.doubtnutapp.domain.common.repository.TestRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestRepositoryImpl @Inject constructor() : TestRepository {

    private val attemptedQuizIds = mutableListOf<AttemptedTestModel>()

    override fun addQuizIdToAttemptedList(testId: Int, testSubscriptionId: Int): Completable = Completable.create {
        attemptedQuizIds.add(AttemptedTestModel(testId, testSubscriptionId))
        it.onComplete()
    }

    override fun isQuizAttempted(testId: Int): Single<AttemptedTest> = Single.create<AttemptedTest> { emitter ->

        val attemptedTest = attemptedQuizIds.firstOrNull {
            it.testId == testId
        }

        attemptedTest?.let { emitter.onSuccess(AttemptedTest(it.testId, it.subscriptionId)) }
            ?: emitter.onError(Throwable("Quiz Not attempted before"))
    }
}
