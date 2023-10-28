package com.doubtnutapp.domain.common.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.common.entities.model.AttemptedTest
import com.doubtnutapp.domain.common.repository.TestRepository
import io.reactivex.Single
import javax.inject.Inject

class CheckQuizAttempted @Inject constructor(val testRepository: TestRepository) : SingleUseCase<AttemptedTest, CheckQuizAttempted.Param> {

    override fun execute(param: Param): Single<AttemptedTest> = testRepository.isQuizAttempted(param.testId)

    @Keep
    class Param(val testId: Int)
}
