package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.ui.questionAskedHistory.QuestionAskedHistoryDetails
import retrofit2.http.GET
import retrofit2.http.Url

interface QuestionAskedHistoryService {

    @GET
    fun getQuestionAskedHistoryFromURL(@Url url: String): RetrofitLiveData<ApiResponse<QuestionAskedHistoryDetails>>
}
