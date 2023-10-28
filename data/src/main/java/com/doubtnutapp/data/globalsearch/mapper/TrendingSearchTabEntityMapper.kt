package com.doubtnutapp.data.globalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.globalsearch.model.ApiGlobalSearchTab
import com.doubtnutapp.domain.globalsearch.entities.GlobalSearchTabEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrendingSearchTabEntityMapper @Inject constructor() :
    Mapper<ApiGlobalSearchTab, GlobalSearchTabEntity> {
    override fun map(srcObject: ApiGlobalSearchTab) = with(srcObject) {
        GlobalSearchTabEntity(
            description,
            key
        )
    }
}
