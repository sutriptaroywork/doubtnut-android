package com.doubtnutapp.data.mainscreen.apiservice

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.homefeed.model.ApiWebViewData
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MainScreenService {

    @POST("/v2/gamification/updatedailystreak")
    fun registerDailyStreakEvent(): Completable

    @GET("/v1/config/getbottomsheet")
    fun getWebViewData(@Query("student_class") studentClass: String): Single<ApiResponse<ApiWebViewData>>
}
