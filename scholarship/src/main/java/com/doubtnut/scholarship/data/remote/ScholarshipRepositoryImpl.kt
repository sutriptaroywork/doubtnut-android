package com.doubtnut.scholarship.data.remote

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.scholarship.data.entity.ScholarshipData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

open class ScholarshipRepositoryImpl @Inject constructor(
    private val scholarshipService: ScholarshipService
) : ScholarshipRepository {

    override fun getScholarshipData(
        id: String,
        changeTest: Boolean?
    )
            : Flow<ScholarshipData> =
        flow {
            emit(
                scholarshipService.getScholarshipData(id, changeTest).data
            )
        }

    override fun registerScholarshipTest(
        id: String
    )
            : Flow<BaseResponse> =
        flow {
            emit(
                scholarshipService.registerScholarshipTest(id).data
            )
        }

}
