package com.doubtnutapp.domain.newglobalsearch.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.newglobalsearch.entities.SearchSuggestionsDataEntity
import com.doubtnutapp.domain.newglobalsearch.repository.TypeYourDoubtRepository
import io.reactivex.Single
import javax.inject.Inject

class TypeYourDoubtSuggestionsUseCase @Inject constructor(private val typeYouDoubtRepository: TypeYourDoubtRepository) :
    SingleUseCase<SearchSuggestionsDataEntity, TypeYourDoubtSuggestionsUseCase.Params> {

    override fun execute(param: Params): Single<SearchSuggestionsDataEntity> =
        typeYouDoubtRepository
            .getSearchSuggestions(param.text)

    @Keep
    data class Params(val text: String)
}
