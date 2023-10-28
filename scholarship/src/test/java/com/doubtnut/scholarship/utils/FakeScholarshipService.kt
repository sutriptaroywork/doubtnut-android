package com.doubtnut.scholarship.utils

import com.doubtnut.core.data.remote.CoreResponse
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.scholarship.data.entity.ScholarshipData
import com.doubtnut.scholarship.data.remote.ScholarshipService

class FakeScholarshipService: ScholarshipService {


    override suspend fun getScholarshipData(
        id: String,
        changeTest: Boolean?
    ): CoreResponse<ScholarshipData> {
        return TestData.response1
    }

    override suspend fun registerScholarshipTest(id: String): CoreResponse<BaseResponse> {
        return TestData.response2
    }
}