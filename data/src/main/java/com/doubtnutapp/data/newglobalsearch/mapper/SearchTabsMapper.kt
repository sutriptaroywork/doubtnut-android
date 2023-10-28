package com.doubtnutapp.data.newglobalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.newglobalsearch.model.ApiSearchTab
import com.doubtnutapp.domain.newglobalsearch.entities.SearchTabsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchTabsMapper @Inject constructor() : Mapper<List<ApiSearchTab>, List<SearchTabsEntity>> {

    override fun map(srcObject: List<ApiSearchTab>): List<SearchTabsEntity> =
        getTabsEntity(srcObject)

    private fun getTabsEntity(tabList: List<ApiSearchTab>): List<SearchTabsEntity> =
        tabList.map {
            getSearchTabEntity(it)
        }

    private fun getSearchTabEntity(apiSearchTab: ApiSearchTab): SearchTabsEntity =
        with(apiSearchTab) {
            SearchTabsEntity(
                description = description.orEmpty(),
                key = key.orEmpty(),
                isVip = isVip ?: false,
                filterList = filterList
            )
        }
}
