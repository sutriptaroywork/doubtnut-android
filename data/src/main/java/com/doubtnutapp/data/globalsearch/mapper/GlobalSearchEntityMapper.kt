package com.doubtnutapp.data.globalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.globalsearch.model.ApiGlobalSearch
import com.doubtnutapp.data.globalsearch.model.ApiGlobalSearchResult
import com.doubtnutapp.data.globalsearch.model.ApiGlobalSearchTab
import com.doubtnutapp.domain.globalsearch.entities.GlobalSearchEntity
import com.doubtnutapp.domain.globalsearch.entities.GlobalSearchResultEntity
import com.doubtnutapp.domain.globalsearch.entities.GlobalSearchTabEntity
import javax.inject.Inject

class GlobalSearchEntityMapper @Inject constructor(
    private val globalSearchResultEntityMapper: GlobalSearchResultEntityMapper,
    private val globalSearchTabEntityMapper: GlobalSearchTabEntityMapper
) : Mapper<ApiGlobalSearch, GlobalSearchEntity> {
    override fun map(srcObject: ApiGlobalSearch) = with(srcObject) {
        GlobalSearchEntity(
            getSearchTabList(searchTabs),
            getSearchResultTabList(searchResultList)
        )
    }

    private fun getSearchResultTabList(searchResultList: List<ApiGlobalSearchResult>): List<GlobalSearchResultEntity> =
        searchResultList.map {
            globalSearchResultEntityMapper.map(it)
        }

    private fun getSearchTabList(searchTabs: List<ApiGlobalSearchTab>): List<GlobalSearchTabEntity> =
        searchTabs.map {
            globalSearchTabEntityMapper.map(it)
        }
}
