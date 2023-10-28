package com.doubtnutapp.domain.newglobalsearch.repository

import com.doubtnutapp.domain.newglobalsearch.entities.YTSearchDataEntity
import io.reactivex.Single

interface YoutubeSearchRepository {

    fun getYoutubeSearchResults(text: String, youtubeApiKey: String): Single<YTSearchDataEntity>
}
