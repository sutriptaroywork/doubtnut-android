package com.doubtnutapp.data.newglobalsearch.repository

import com.doubtnutapp.data.common.service.ThirdPartyApisService
import com.doubtnutapp.data.newglobalsearch.mapper.YoutubeSearchMapper
import com.doubtnutapp.domain.newglobalsearch.entities.YTSearchDataEntity
import com.doubtnutapp.domain.newglobalsearch.repository.YoutubeSearchRepository
import io.reactivex.Single
import javax.inject.Inject

class YoutubeSearchRepositoryImpl @Inject constructor(
    private val thirdPartyApisService: ThirdPartyApisService,
    private val youtubeSearchMapper: YoutubeSearchMapper
) : YoutubeSearchRepository {

    override fun getYoutubeSearchResults(text: String, youtubeApiKey: String):
        Single<YTSearchDataEntity> {
        return thirdPartyApisService
            .getYoutubeSearchResult(
                text, "snippet", "15", "video",
                "strict", youtubeApiKey
            )
            .map {
                youtubeSearchMapper.map(it)
            }
    }
}
