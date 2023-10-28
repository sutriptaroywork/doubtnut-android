package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.MicroService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.userstatus.StatusApiResponse
import io.reactivex.Single

class UserStatusRepository(val microService: MicroService) {
    fun getUserStories(page: Int, type: String, offsetCursor: String) =
        microService.getStories(page, type, offsetCursor)

    fun getPopularStories(page: Int, offsetCursor: String): Single<ApiResponse<StatusApiResponse>> =
        microService.getPopularStories(page, offsetCursor)

    fun getStatusAds(): Single<ApiResponse<StatusApiResponse>> = microService.getStatusAds()
}
