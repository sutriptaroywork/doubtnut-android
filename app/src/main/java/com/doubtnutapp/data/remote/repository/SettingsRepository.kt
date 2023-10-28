package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.SettingsService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Settings

class SettingsRepository(val settingsService: SettingsService) {

    fun aboutus(): RetrofitLiveData<ApiResponse<Settings>> = settingsService.aboutus()
    fun contactus(): RetrofitLiveData<ApiResponse<Settings>> = settingsService.contactus()
    fun termsnconditions(): RetrofitLiveData<ApiResponse<Settings>> =
        settingsService.termsnconditions()

    fun privacy(): RetrofitLiveData<ApiResponse<Settings>> = settingsService.privacypolicy()
    fun cameraGuide(): RetrofitLiveData<ApiResponse<Any>> = settingsService.cameraGuide()
}
