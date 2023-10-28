package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.home.model.ExploreMoreWidgetResponse
import retrofit2.http.GET

/**
 * Created by Mehul Bisht on 05/01/22
 */
interface ExploreMoreWidgetService {

    @GET("v1/homepage/last-watched-question-widget")
    suspend fun getExploreMoreWidget(): ApiResponse<ExploreMoreWidgetResponse>
}
