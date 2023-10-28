package com.doubtnutapp.data.gamification.leaderboard.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.gamification.leaderboard.model.ApiLeaderboardData
import io.reactivex.Single
import retrofit2.http.GET

interface GameLeaderBoardService {

    @GET("/v2/gamification/leaderboard")
    fun getGameLeaders(): Single<ApiResponse<ApiLeaderboardData>>
}
