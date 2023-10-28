package com.doubtnutapp.resultpage.repository

import com.doubtnutapp.resultpage.model.ResultPageData
import kotlinx.coroutines.flow.Flow

interface ResultPageRepository {

    fun getResultPageData(
        page: String?,
        type:String?,
        source:String?
    )
            : Flow<ResultPageData>

}