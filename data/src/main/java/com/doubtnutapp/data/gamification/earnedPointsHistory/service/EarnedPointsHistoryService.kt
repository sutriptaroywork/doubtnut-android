package com.doubtnutapp.data.gamification.earnedPointsHistory.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.gamification.earnedPointsHistory.model.ApiEarnedPointsHistoryList
import io.reactivex.Single
import retrofit2.http.GET

/**
 *  Created by Pradip Awasthi on 2019-10-22.
 */

interface EarnedPointsHistoryService {
    @GET("v2/gamification/pointshistory")
    fun getUserMilestonesAndGameActions(): Single<ApiResponse<List<ApiEarnedPointsHistoryList>>>
}
