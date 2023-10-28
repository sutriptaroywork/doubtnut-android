package com.doubtnutapp.domain.common.repository

import com.doubtnutapp.domain.common.entities.model.AttemptedTest
import io.reactivex.Completable
import io.reactivex.Single

interface TestRepository {
    fun addQuizIdToAttemptedList(testId: Int, testSubscriptionId: Int): Completable
    fun isQuizAttempted(testId: Int): Single<AttemptedTest>
}
