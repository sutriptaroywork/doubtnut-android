package com.doubtnutapp.newglobalsearch.mapper

import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.newglobalsearch.entities.SearchSuggestionEntity
import com.doubtnutapp.domain.newglobalsearch.entities.SearchSuggestionsDataEntity
import com.doubtnutapp.newglobalsearch.model.SearchListViewItem
import com.doubtnutapp.newglobalsearch.model.SearchSuggestionItem
import com.doubtnutapp.newglobalsearch.model.SearchSuggestionsDataItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchSuggestionsItemMapper @Inject constructor() : Mapper<SearchSuggestionsDataEntity, SearchSuggestionsDataItem> {

    override fun map(srcObject: SearchSuggestionsDataEntity): SearchSuggestionsDataItem =
        SearchSuggestionsDataItem(getSuggestionsList(srcObject.suggestionsList))

    private fun getSuggestionsList(suggestionsPlaylist: MutableList<SearchSuggestionEntity>): MutableList<SearchListViewItem> =
        suggestionsPlaylist.map {
            getSearchSuggestionItem(it)
        }.distinctBy {
            Pair(it.displayText, it.displayText)
        }.toMutableList()

    private fun getSearchSuggestionItem(trendingSearchPlaylistEntity: SearchSuggestionEntity): SearchSuggestionItem =
        with(trendingSearchPlaylistEntity) {
            SearchSuggestionItem(
                displayText = displayText,
                variantId = variantId,
                id = id,
                version = suggestionVersion.orEmpty(),
                viewType = R.layout.item_search_suggestion,
                fakeType = displayText
            )
        }
}