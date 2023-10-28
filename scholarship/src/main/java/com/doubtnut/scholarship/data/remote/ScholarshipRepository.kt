package com.doubtnut.scholarship.data.remote

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.scholarship.data.entity.ScholarshipData
import kotlinx.coroutines.flow.Flow

interface ScholarshipRepository {
    fun getScholarshipData(
        id: String,
        changeTest: Boolean?
    )
            : Flow<ScholarshipData>

    fun registerScholarshipTest(
        id: String
    )
            : Flow<BaseResponse>
}