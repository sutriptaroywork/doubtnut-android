package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.ContestList
import com.doubtnutapp.data.remote.models.DailyPrize
import retrofit2.http.GET
import retrofit2.http.Path

interface DailyPrizeService {

    @GET("/v3/contest/get/{contest_id}")
    fun getDailyPrize(@Path("contest_id") contestId: String): RetrofitLiveData<ApiResponse<DailyPrize>>

    @GET("/v3/contest/get-active")
    fun getContestList(): RetrofitLiveData<ApiResponse<ArrayList<ContestList>>>
}
