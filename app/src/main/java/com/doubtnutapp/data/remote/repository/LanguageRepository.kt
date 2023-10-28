package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.LanguageService
import com.doubtnutapp.data.remote.models.ApiLanguage
import com.doubtnutapp.data.remote.models.ApiResponse
import okhttp3.RequestBody

class LanguageRepository(val languageService: LanguageService) {

    fun getLanguages(udid: String): RetrofitLiveData<ApiResponse<ApiLanguage>> =
        languageService.getLanguages(udid)

    fun setLanguage(body: RequestBody): RetrofitLiveData<ApiResponse<Any>> =
        languageService.setLanguage(body)
}
