package com.doubtnutapp.examcorner.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.examcorner.model.ApiExamCornerBookmarkData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class ExamCornerBookmarkRepository @Inject constructor(private val networkService: NetworkService) {

    fun fetchExamCornerBookmarkData(page: Int)
            : Flow<ApiResponse<ApiExamCornerBookmarkData>> =
        flow { emit(networkService.getExamCornerBookmarkData(page)) }

    fun setExamCornerBookmark(id: String, state: Boolean)
            : Flow<ApiResponse<Unit>> =
        flow {
            emit(
                networkService.setExamCornerBookmark(
                    hashMapOf(
                        "exam_corner_id" to id, "type" to if (state) {
                            "add"
                        } else {
                            "remove"
                        }
                    ).toRequestBody()
                )
            )
        }

}