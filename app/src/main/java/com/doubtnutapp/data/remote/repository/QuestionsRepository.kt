package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.QuestionMeta

class QuestionsRepository(val networkService: NetworkService) {

    fun getQuestionByTag(
        page: String,
        body: String
    ): RetrofitLiveData<ApiResponse<ArrayList<QuestionMeta>>> =
        networkService.getQuestionByTag(page, body)
}
