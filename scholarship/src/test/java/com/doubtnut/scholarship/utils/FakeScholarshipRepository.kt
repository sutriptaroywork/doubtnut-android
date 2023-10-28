package com.doubtnut.scholarship.utils

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.scholarship.data.entity.ScholarshipData
import com.doubtnut.scholarship.data.remote.ScholarshipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeScholarshipRepository : ScholarshipRepository {

    override fun getScholarshipData(id: String, changeTest: Boolean?): Flow<ScholarshipData> {
        return flowOf(TestData.scholarshipData)
    }

    override fun registerScholarshipTest(id: String): Flow<BaseResponse> {
        return flowOf()
    }
}