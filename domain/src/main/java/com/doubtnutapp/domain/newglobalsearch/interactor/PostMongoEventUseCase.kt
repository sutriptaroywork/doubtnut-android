package com.doubtnutapp.domain.newglobalsearch.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.newglobalsearch.repository.TrendingSearchRepository
import io.reactivex.Completable
import javax.inject.Inject

class PostMongoEventUseCase @Inject constructor(
    private val trendingSearchRepository: TrendingSearchRepository
) : CompletableUseCase<PostMongoEventUseCase.Param> {

    override fun execute(param: Param): Completable =
        trendingSearchRepository
            .postMongoEvent(param.paramsMap)

    @Keep
    data class Param(
        val paramsMap: Map<String, Any>
    )
}
