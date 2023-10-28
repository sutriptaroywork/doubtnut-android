package com.doubtnutapp.data.gamification.gamePoints.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.gamification.gamePoints.model.ApiGamePoints
import io.reactivex.Single
import retrofit2.http.GET

interface GamePointsService {

    @GET("/v2/gamification/points")
    fun getUserMilestonesAndGameActions(): Single<ApiResponse<ApiGamePoints>>
}
