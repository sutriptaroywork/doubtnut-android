package com.doubtnutapp.domain.similarVideo.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.similarVideo.entities.SimilarVideoEntity
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import io.reactivex.Single
import javax.inject.Inject

class GetPreviousSimilarVideoInteractor @Inject constructor(private val similarVideoRepository: SimilarVideoRepository) : SingleUseCase<SimilarVideoEntity, GetPreviousSimilarVideoInteractor.None> {

    override fun execute(param: None): Single<SimilarVideoEntity> = similarVideoRepository.getPreviousSimilarVideo()

    class None
}
