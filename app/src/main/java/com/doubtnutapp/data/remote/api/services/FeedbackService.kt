package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ActiveFeedback
import com.doubtnutapp.data.remote.models.ApiResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeedbackService {

    @GET("v2/feedback/get-active")
    fun getActiveFeedbacks(): RetrofitLiveData<ApiResponse<ArrayList<ActiveFeedback>>>

    @POST("/v2/feedback/submit")
    fun updateFeedbackResponse(@Body body: RequestBody): RetrofitLiveData<ApiResponse<ResponseBody>>
}
