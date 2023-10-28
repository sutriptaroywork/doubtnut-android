package com.doubtnutapp.domain.similarVideo.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistEntity
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import io.reactivex.Single
import javax.inject.Inject

class GetSimilarPlaylistUseCase @Inject constructor(private val similarVideoRepository: SimilarVideoRepository) : SingleUseCase<SimilarPlaylistEntity, String> {

    override fun execute(param: String): Single<SimilarPlaylistEntity> = similarVideoRepository.getSimilarPlaylist(param)
}
