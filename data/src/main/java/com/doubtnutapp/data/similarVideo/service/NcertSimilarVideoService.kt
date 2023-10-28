package com.doubtnutapp.data.similarVideo.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.domain.similarVideo.entities.ApiNcertSimilar
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface NcertSimilarVideoService {

    @POST("v1/answers/ncert-videos-additional-data")
    fun getNcertVideoAdditionalData(
        @Body requestBody: RequestBody
    ): Single<ApiResponse<ApiNcertSimilar>>

    @PUT("v1/student/ncert-last-watched-details")
    fun postNcertLastWatchedDetails(
        @Body requestBody: RequestBody
    ): Completable
}
