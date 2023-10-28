package com.doubtnutapp.feed

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.feed.entity.TopIconsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class TopIconsRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun getAllHomeTopIcons(
        screen: String,
        userAssortment: String,
        screenWidth: Int?
    ): Flow<TopIconsData> =
        flow {
            emit(
                networkService.getAllHomeTopIcons(
                    screen = screen,
                    userAssortment = userAssortment,
                    screenWidth = screenWidth
                ).data
            )
        }
}
