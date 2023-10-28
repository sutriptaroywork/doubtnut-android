package com.doubtnutapp.data.globalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.globalsearch.model.ApiGlobalSearchTab
import com.doubtnutapp.data.globalsearch.model.ApiTrendingSearch
import com.doubtnutapp.data.globalsearch.model.ApiTrendingSearchResult
import com.doubtnutapp.domain.globalsearch.entities.GlobalSearchEntity
import com.doubtnutapp.domain.globalsearch.entities.GlobalSearchResultEntity
import com.doubtnutapp.domain.globalsearch.entities.GlobalSearchTabEntity
import javax.inject.Inject

class TrendingSearchEntityMapper @Inject constructor(
    private val trendingSearchResultEntityMapper: TrendingSearchResultEntityMapper,
    private val trendingSearchTabEntityMapper: TrendingSearchTabEntityMapper
) : Mapper<ApiTrendingSearch, GlobalSearchEntity> {
    override fun map(srcObject: ApiTrendingSearch) = with(srcObject) {
        GlobalSearchEntity(
            getSearchTabList(searchTabs),
            getSearchResultTabList(searchResultList)
        )
    }

    private fun getSearchResultTabList(searchResultList: List<ApiTrendingSearchResult>): List<GlobalSearchResultEntity> =
        searchResultList.map {
            trendingSearchResultEntityMapper.map(it)
        }

    private fun getSearchTabList(searchTabs: List<ApiGlobalSearchTab>): List<GlobalSearchTabEntity> =
        searchTabs.map {
            trendingSearchTabEntityMapper.map(it)
        }
}
