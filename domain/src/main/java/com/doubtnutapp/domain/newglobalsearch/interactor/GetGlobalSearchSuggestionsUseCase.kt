package com.doubtnutapp.domain.newglobalsearch.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.newglobalsearch.entities.SearchSuggestionsDataEntity
import com.doubtnutapp.domain.newglobalsearch.repository.TrendingSearchRepository
import io.reactivex.Single
import javax.inject.Inject

class GetGlobalSearchSuggestionsUseCase @Inject constructor(private val trendingSearchRepository: TrendingSearchRepository) :
    SingleUseCase<SearchSuggestionsDataEntity, GetGlobalSearchSuggestionsUseCase.Params> {

    override fun execute(param: Params): Single<SearchSuggestionsDataEntity> =
        trendingSearchRepository
            .getSearchSuggestions(param.text, param.suggesterPayloadMap, param.source)

    @Keep
    data class Params(
        val text: String,
        val suggesterPayloadMap: HashMap<String, Any>?,
        val source: String
    )
}
