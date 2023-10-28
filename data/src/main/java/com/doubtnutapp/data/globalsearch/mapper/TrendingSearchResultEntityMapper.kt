package com.doubtnutapp.data.globalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.globalsearch.model.ApiTrendingSearchResult
import com.doubtnutapp.domain.globalsearch.entities.GlobalSearchResultEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrendingSearchResultEntityMapper @Inject constructor() :
    Mapper<ApiTrendingSearchResult, GlobalSearchResultEntity> {
    override fun map(srcObject: ApiTrendingSearchResult) = with(srcObject) {
        GlobalSearchResultEntity(
            resultType,
            null,
            title,
            resultCourse,
            resultClass,
            chapter,
            null,
            null,
            null
        )
    }
}
