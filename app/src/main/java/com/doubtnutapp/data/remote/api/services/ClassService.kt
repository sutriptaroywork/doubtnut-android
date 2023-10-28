package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.StudentClass
import retrofit2.http.GET
import retrofit2.http.Path

interface ClassService {

    @GET("v4/class/get-list/{lng_code}")
    fun getClassesWithSSC(@Path("lng_code") languageCode: String): RetrofitLiveData<ApiResponse<ArrayList<StudentClass>>>
}
