package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.FeedbackService
import com.doubtnutapp.data.remote.models.ActiveFeedback
import com.doubtnutapp.data.remote.models.ApiResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody

class FeedbackRepository(val feedbackService: FeedbackService) {

    fun getActiveFeedbacks(): RetrofitLiveData<ApiResponse<ArrayList<ActiveFeedback>>> =
        feedbackService.getActiveFeedbacks()

    fun updateFeedbackResponse(
        params: RequestBody
    ): RetrofitLiveData<ApiResponse<ResponseBody>> = feedbackService.updateFeedbackResponse(params)
}
