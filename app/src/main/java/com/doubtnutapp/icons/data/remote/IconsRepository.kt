package com.doubtnutapp.icons.data.remote

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.icons.data.entity.IconsDetailResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class IconsRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun increaseIconsCount(id: String)
            : Flow<Unit> =
        flow {
            networkService.increaseIconsCount(id)
        }

    fun getCategories(
        type: String,
        id: String,
        page: Int,
    )
            : Flow<IconsDetailResponse> =
        flow {
            emit(
                networkService.getCategories(
                    type = type,
                    id = id,
                    page = page
                ).data
            )
        }
}