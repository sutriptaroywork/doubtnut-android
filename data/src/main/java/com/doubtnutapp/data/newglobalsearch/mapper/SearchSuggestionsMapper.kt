package com.doubtnutapp.data.newglobalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.newglobalsearch.model.ApiSuggestionData
import com.doubtnutapp.data.newglobalsearch.model.Suggestion
import com.doubtnutapp.domain.newglobalsearch.entities.SearchSuggestionEntity
import com.doubtnutapp.domain.newglobalsearch.entities.SearchSuggestionsDataEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchSuggestionsMapper @Inject constructor() :
    Mapper<ApiSuggestionData, SearchSuggestionsDataEntity> {

    override fun map(srcObject: ApiSuggestionData): SearchSuggestionsDataEntity =
        SearchSuggestionsDataEntity(getSuggestionsData(srcObject))

    private fun getSuggestionsData(suggestionsList: ApiSuggestionData): MutableList<SearchSuggestionEntity> =
        suggestionsList.data.map {
            getSearchSuggestionEntity(suggestionsList.suggestionVersion.orEmpty(), it)
        }.toMutableList()

    private fun getSearchSuggestionEntity(
        version: String,
        apiSearchSuggestion: Suggestion
    ): SearchSuggestionEntity =
        with(apiSearchSuggestion) {
            SearchSuggestionEntity(
                displayText = display.orEmpty(),
                variantId = variantId,
                id = id.orEmpty(),
                suggestionVersion = version
            )
        }
}
