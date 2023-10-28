package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.whatsappadmin.StateDistrictApiResponse
import com.doubtnutapp.data.remote.models.whatsappadmin.WhatsappAdminInfo
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WhatsAppAdminService {

    @POST("v1/district/postform")
    fun submitWhatsappAdminForm(@Body requestBody: RequestBody): Single<ApiResponse<Any>>

    @GET("v1/district/homepage")
    fun fetchWhatsappAdminInfo(): Single<ApiResponse<WhatsappAdminInfo>>

    @GET("v1/district/districts")
    fun fetchStateAndDistrict(): Single<StateDistrictApiResponse>
}
