package com.doubtnutapp.data.textsolution.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.textsolution.model.ApiTextSolutionData
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
interface TextSolutionService {

    @POST("v13/answers/view-answer-by-question-id")
    fun getVideo(@Body params: RequestBody): Single<ApiResponse<ApiTextSolutionData>>

    @POST("v4/feedback/video-add")
    fun videoLikedDisliked(@Body requestBody: RequestBody): Completable

    @POST("v1/sharing/whatsapp")
    fun videoShared(@Body requestBody: RequestBody): Completable

    @POST("/v10/answers/update-answer-view")
    fun updateVideoView(@Body bodyParam: RequestBody): Completable

    @PUT("v1/answers/store-text-solution-feedback")
    fun requestVideoSolution(@Body bodyParam: RequestBody): Completable
}
