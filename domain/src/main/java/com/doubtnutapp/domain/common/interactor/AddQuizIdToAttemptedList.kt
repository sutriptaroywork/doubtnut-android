package com.doubtnutapp.domain.common.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.common.repository.TestRepository
import io.reactivex.Completable
import javax.inject.Inject

class AddQuizIdToAttemptedList @Inject constructor(private val testRepository: TestRepository) : CompletableUseCase<AddQuizIdToAttemptedList.Param> {

    override fun execute(param: Param): Completable = testRepository.addQuizIdToAttemptedList(param.testId, param.testSubscriptionId)

    @Keep
    class Param(val testId: Int, val testSubscriptionId: Int)
}
