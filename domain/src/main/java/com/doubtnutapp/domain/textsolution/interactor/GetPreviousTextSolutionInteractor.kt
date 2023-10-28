package com.doubtnutapp.domain.textsolution.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.textsolution.entities.TextSolutionDataEntity
import com.doubtnutapp.domain.textsolution.repository.TextSolutionRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
class GetPreviousTextSolutionInteractor @Inject constructor(private val textSolutionRepository: TextSolutionRepository) : SingleUseCase<TextSolutionDataEntity, GetPreviousTextSolutionInteractor.None> {

    override fun execute(param: None): Single<TextSolutionDataEntity> = textSolutionRepository.getPreviousVideo()

    class None
}
