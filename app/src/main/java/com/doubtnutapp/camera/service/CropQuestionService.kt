package com.doubtnutapp.camera.service

import com.doubtnutapp.data.camerascreen.model.ApiCropScreenConfig
import com.doubtnutapp.data.common.model.ApiResponse
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Created by Sachin Saxena on 2020-10-19.
 */
interface CropQuestionService {

    @GET("/v1/config/demo")
    fun getCropScreenConfig(): Single<ApiResponse<ApiCropScreenConfig>>

    @POST("/v1/camera/post-face-data")
    fun saveSelfieDetectedImage(@Body params: RequestBody): Completable
}
