package com.doubtnutapp.data.globalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.globalsearch.model.ApiGlobalSearchResult
import com.doubtnutapp.domain.globalsearch.entities.GlobalSearchResultEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalSearchResultEntityMapper @Inject constructor() : Mapper<ApiGlobalSearchResult, GlobalSearchResultEntity> {
    override fun map(srcObject: ApiGlobalSearchResult) = with(srcObject) {
        GlobalSearchResultEntity(
            data.resultType,
            data.questionId,
            data.title,
            data.resultCourse,
            data.resultClass,
            data.chapter,
            data.microConceptId,
            data.subtopic,
            data.page
        )
    }
}
