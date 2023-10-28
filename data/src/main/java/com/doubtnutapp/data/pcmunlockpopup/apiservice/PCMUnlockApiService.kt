package com.doubtnutapp.data.pcmunlockpopup.apiservice

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.pcmunlockpopup.model.ApiPCMUnlockData
import io.reactivex.Single
import retrofit2.http.GET

interface PCMUnlockApiService {

    @GET("/v1/gamification/unlockinfo")
    fun getPCMUnlockData(): Single<ApiResponse<ApiPCMUnlockData>>
}
