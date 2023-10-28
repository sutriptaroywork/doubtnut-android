package com.doubtnut.olympiad.data.remote

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.olympiad.data.entity.OlympiadDetailResponse
import com.doubtnut.olympiad.data.entity.OlympiadSuccessResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OlympiadRepository @Inject constructor(
    private val olympiadService: OlympiadService
) {

    fun getOlympiadDetails(
        type: String,
        id: String,
    )
            : Flow<OlympiadDetailResponse> =
        flow {
            emit(
                olympiadService.getOlympiadDetails(
                    type = type,
                    id = id
                ).data
            )
        }

    fun postOlympiadRegister(
        type: String,
        id: String,
        payload: Map<String, String>,
    )
            : Flow<BaseResponse> =
        flow {
            emit(
                olympiadService.postOlympiadRegister(
                    type = type,
                    id = id,
                    payload = payload
                ).data
            )
        }

    fun getOlympiadSuccessData(
        type: String,
        id: String,
    )
            : Flow<OlympiadSuccessResponse> =
        flow {
            emit(
                olympiadService.getOlympiadSuccessData(
                    type = type,
                    id = id
                ).data
            )
        }
}