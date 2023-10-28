package com.doubtnutapp.domain.quizLibrary.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.quizLibrary.entities.QuizDetailsEntity
import com.doubtnutapp.domain.quizLibrary.repository.DailyQuizRepository
import io.reactivex.Single
import javax.inject.Inject

class GetQuizUseCase @Inject constructor(private val quizRepository: DailyQuizRepository) :
    SingleUseCase<List<QuizDetailsEntity>, GetQuizUseCase.Param> {

    override fun execute(param: Param): Single<List<QuizDetailsEntity>> = quizRepository.getQuizData(param.page)

    @Keep
    class Param(val page: Int)
}
