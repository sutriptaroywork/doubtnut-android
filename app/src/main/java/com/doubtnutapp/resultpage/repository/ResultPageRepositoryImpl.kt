package com.doubtnutapp.resultpage.repository

import com.doubtnutapp.resultpage.model.ResultPageData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ResultPageRepositoryImpl @Inject constructor(
    private val resultPageService: ResultPageService
) : ResultPageRepository {

    override fun getResultPageData(
        page: String?,
        type: String?,
        source: String?
    ): Flow<ResultPageData> = flow {
        emit(
            resultPageService.getResultPageData(page,type,source).data
        )
    }

}