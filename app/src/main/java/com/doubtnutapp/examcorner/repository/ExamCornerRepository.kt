package com.doubtnutapp.examcorner.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.examcorner.model.ApiExamCornerData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class ExamCornerRepository @Inject constructor(private val networkService: NetworkService) {

    fun fetchExamCornerData(filterType: String, page: Int)
            : Flow<ApiResponse<ApiExamCornerData>> =
        flow { emit(networkService.getExamCornerData(filterType, page)) }

}