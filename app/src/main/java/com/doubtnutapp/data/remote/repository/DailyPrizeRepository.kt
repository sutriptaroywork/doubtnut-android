package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.DailyPrizeService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.ContestList
import com.doubtnutapp.data.remote.models.DailyPrize

class DailyPrizeRepository(private val dailyPrizeService: DailyPrizeService) {

    fun getDailyPrize(token: String, contestId: String): RetrofitLiveData<ApiResponse<DailyPrize>> =
        dailyPrizeService.getDailyPrize(contestId)

    fun getContestList(token: String): RetrofitLiveData<ApiResponse<ArrayList<ContestList>>> =
        dailyPrizeService.getContestList()
}
