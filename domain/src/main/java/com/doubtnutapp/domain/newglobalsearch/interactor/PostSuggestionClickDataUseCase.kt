package com.doubtnutapp.domain.newglobalsearch.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.newglobalsearch.repository.TrendingSearchRepository
import io.reactivex.Completable
import javax.inject.Inject

class PostSuggestionClickDataUseCase @Inject constructor(
    private val trendingSearchRepository: TrendingSearchRepository
) : CompletableUseCase<PostSuggestionClickDataUseCase.Param> {

    override fun execute(param: Param): Completable =
        trendingSearchRepository
            .postSuggestionClickData(param.paramsMap)

    @Keep
    data class Param(
        val paramsMap: Map<String, Any>
    )
}
