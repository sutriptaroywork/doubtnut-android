package com.doubtnutapp.domain.textsolution.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.textsolution.repository.TextSolutionRepository
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
class ShareTextSolutionInteractor @Inject constructor(private val textSolutionRepository: TextSolutionRepository) : CompletableUseCase<String> {

    override fun execute(param: String): Completable = textSolutionRepository.videoShared(param)
}
