package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.TopDoubtQuestion
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 24/11/20.
 */
class TopDoubtRepository @Inject constructor(private val networkService: NetworkService) {

    fun fetchTopDoubtQuestions(
        entityType: String,
        entityId: String,
        page: String,
        batchId: String?
    ): Flow<ApiResponse<List<TopDoubtQuestion>?>> =
        flow {
            emit(
                networkService.fetchTopDoubtQuestions(
                    entityType,
                    entityId,
                    page,
                    "top_doubts",
                    batchId
                )
            )
        }

    fun fetchTopDoubtAnswerData(entityId: String, batchId: String?): Flow<ApiResponse<List<WidgetEntityModel<*, *>>>> =
        flow { emit(networkService.fetchTopDoubtAnswerData(entityId, batchId)) }
}
