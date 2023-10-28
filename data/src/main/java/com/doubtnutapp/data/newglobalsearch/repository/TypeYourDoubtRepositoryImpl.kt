package com.doubtnutapp.data.newglobalsearch.repository

import android.content.SharedPreferences
import com.doubtnutapp.data.newglobalsearch.mapper.SearchSuggestionsMapper
import com.doubtnutapp.data.newglobalsearch.service.TypeYourDoubtService
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.newglobalsearch.entities.SearchSuggestionsDataEntity
import com.doubtnutapp.domain.newglobalsearch.repository.TypeYourDoubtRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class TypeYourDoubtRepositoryImpl @Inject constructor(
    private val typeYourDoubtService: TypeYourDoubtService,
    private val searchSuggestionMapper: SearchSuggestionsMapper,
    private val sharedPreferences: SharedPreferences
) : TypeYourDoubtRepository {

    override fun postMongoEvent(paramsMap: Map<String, Any>): Completable {
        return typeYourDoubtService.postUserSearchData(paramsMap.toRequestBody())
    }

    override fun getSearchSuggestions(text: String): Single<SearchSuggestionsDataEntity> {
        val paramsMap = hashMapOf<String, Any>().apply {
            put("text", text)
        }
        return typeYourDoubtService
            .getTydSuggestions(paramsMap.toRequestBody())
            .map {
                searchSuggestionMapper.map(it.data)
            }
    }
}
