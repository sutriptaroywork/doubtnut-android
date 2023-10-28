package com.doubtnutapp.domain.textsolution.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.textsolution.repository.TextSolutionRepository
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
class RequestVideoSolutionInteractor @Inject constructor(private val textSolutionRepository: TextSolutionRepository) :
    CompletableUseCase<RequestVideoSolutionInteractor.Param> {

    override fun execute(param: Param): Completable =
        textSolutionRepository.requestVideoSolution(param.questionId)

    @Keep
    class Param(val questionId: String)
}
