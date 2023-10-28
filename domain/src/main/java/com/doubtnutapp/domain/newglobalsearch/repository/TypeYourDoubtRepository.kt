package com.doubtnutapp.domain.newglobalsearch.repository

import com.doubtnutapp.domain.newglobalsearch.entities.SearchSuggestionsDataEntity
import io.reactivex.Completable
import io.reactivex.Single

interface TypeYourDoubtRepository {

    fun getSearchSuggestions(text: String): Single<SearchSuggestionsDataEntity>

    fun postMongoEvent(paramsMap: Map<String, Any>): Completable
}
