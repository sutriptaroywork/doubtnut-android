package com.doubtnutapp.data.gamification.gamificationmilestone.repository

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.gamification.gamificationmilestone.model.ApiMilestonesAndActions
import io.reactivex.Single
import retrofit2.http.GET

interface MilestoneAndActionService {

    @GET("/v2/gamification/points")
    fun getUserMilestonesAndGameActions(): Single<ApiResponse<ApiMilestonesAndActions>>
}
