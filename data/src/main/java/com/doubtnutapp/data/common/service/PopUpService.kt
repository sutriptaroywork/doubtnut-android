package com.doubtnutapp.data.common.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.domain.common.entities.model.ApiPopUpData
import io.reactivex.Single
import retrofit2.http.GET

interface PopUpService {

    @GET("/v1/homepage/pop-up")
    fun getPopUpList(): Single<ApiResponse<ApiPopUpData>>
}
