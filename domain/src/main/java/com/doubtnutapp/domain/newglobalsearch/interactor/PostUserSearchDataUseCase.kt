package com.doubtnutapp.domain.newglobalsearch.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.newglobalsearch.repository.TrendingSearchRepository
import io.reactivex.Completable
import javax.inject.Inject

class PostUserSearchDataUseCase @Inject constructor(
    private val trendingSearchRepository: TrendingSearchRepository
) : CompletableUseCase<PostUserSearchDataUseCase.Param> {

    override fun execute(param: Param): Completable =
        trendingSearchRepository
            .postUserSearchData(param.searchText, param.size, param.playlistEntity)

    @Keep
    data class Param(
        val searchText: String,
        val size: Int,
        val playlistEntity: String
    )
}
