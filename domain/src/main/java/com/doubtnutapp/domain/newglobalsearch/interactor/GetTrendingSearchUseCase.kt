package com.doubtnutapp.domain.newglobalsearch.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.newglobalsearch.entities.TrendingSearchDataListEntity
import com.doubtnutapp.domain.newglobalsearch.repository.TrendingSearchRepository
import io.reactivex.Single
import javax.inject.Inject

class GetTrendingSearchUseCase @Inject constructor(private val trendingSearchRepository: TrendingSearchRepository) :
    SingleUseCase<List<TrendingSearchDataListEntity>, GetTrendingSearchUseCase.Params> {

    override fun execute(param: Params): Single<List<TrendingSearchDataListEntity>> =
        trendingSearchRepository.getTrendingSearch(param.liveNowTopTap, param.source, param.isTrendingChapterEnabled, param.isVideoQueryChangeEnabled)

    @Keep
    data class Params(
        val liveNowTopTap: Boolean,
        val source: String,
        val isTrendingChapterEnabled: Double,
        val isVideoQueryChangeEnabled: Double
    )
}
