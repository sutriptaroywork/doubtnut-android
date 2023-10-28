package com.doubtnutapp.data.quizLibrary.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.quizLibrary.model.ApiQuizDetails
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface DailyQuizService {

    @GET("v9/testseries/active")
    fun getQuizData(
        @Query("page") page: Int

    ): Single<ApiResponse<List<ApiQuizDetails>>>
}
