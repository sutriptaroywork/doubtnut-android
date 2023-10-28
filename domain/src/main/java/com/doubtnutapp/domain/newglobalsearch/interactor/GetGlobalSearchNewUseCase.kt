package com.doubtnutapp.domain.newglobalsearch.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.newglobalsearch.entities.NewSearchDataEntity
import com.doubtnutapp.domain.newglobalsearch.repository.TrendingSearchRepository
import io.reactivex.Single
import javax.inject.Inject

class GetGlobalSearchNewUseCase @Inject constructor(private val trendingSearchRepository: TrendingSearchRepository) :
    SingleUseCase<NewSearchDataEntity, GetGlobalSearchNewUseCase.Params> {

    override fun execute(param: Params): Single<NewSearchDataEntity> =
        trendingSearchRepository
            .getNewGlobalSearch(
                param.text, param.selectedClass, param.isVoiceSearch,
                param.liveOrder, param.searchTrigger, param.appliedFilterMap, param.source, param.ias_advanced_filter,
                param.advancedFilterTabType
            )

    @Keep
    data class Params(
        val text: String,
        val selectedClass: String,
        val isVoiceSearch: Boolean,
        val liveOrder: Boolean,
        val searchTrigger: String?,
        val appliedFilterMap: java.util.HashMap<String, Any>?,
        val source: String,
        val ias_advanced_filter: java.util.HashMap<String, Any>?,
        val advancedFilterTabType: String
    )
}
