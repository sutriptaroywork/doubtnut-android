package com.doubtnutapp.domain.similarVideo.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.similarVideo.entities.SimilarVideoEntity
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import io.reactivex.Completable
import javax.inject.Inject

class SaveSimilarVideoInteractor @Inject constructor(private val similarVideoRepository: SimilarVideoRepository) : CompletableUseCase<SaveSimilarVideoInteractor.Param> {

    override fun execute(param: Param): Completable = similarVideoRepository.saveSimilarVideoData(param.similarVideoEntity)

    @Keep
    class Param(val similarVideoEntity: SimilarVideoEntity)
}
