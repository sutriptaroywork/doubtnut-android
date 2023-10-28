package com.doubtnutapp.data.library.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.library.model.ApiClassListResponse
import io.reactivex.Single
import retrofit2.http.GET

interface LibraryHomeFragmentService {

    @GET("/v4/class/list")
    fun getClassesList(): Single<ApiResponse<ApiClassListResponse>>
}
