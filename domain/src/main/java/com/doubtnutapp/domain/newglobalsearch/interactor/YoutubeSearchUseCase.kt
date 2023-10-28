package com.doubtnutapp.domain.newglobalsearch.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.newglobalsearch.entities.YTSearchDataEntity
import com.doubtnutapp.domain.newglobalsearch.repository.YoutubeSearchRepository
import io.reactivex.Single
import javax.inject.Inject

class YoutubeSearchUseCase @Inject constructor(private val youtubeSearchRepository: YoutubeSearchRepository) :
    SingleUseCase<YTSearchDataEntity, YoutubeSearchUseCase.Params> {

    override fun execute(param: Params): Single<YTSearchDataEntity> =
        youtubeSearchRepository
            .getYoutubeSearchResults(param.text, param.youtubeApiKey)

    @Keep
    data class Params(val text: String, val youtubeApiKey: String)
}
