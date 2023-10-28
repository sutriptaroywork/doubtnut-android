package com.doubtnutapp.data.camerascreen.apiService

import com.doubtnutapp.data.camerascreen.model.ApiCropScreenConfig
import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.domain.camerascreen.entity.DemoAnimationEntity
import com.doubtnutapp.domain.camerascreen.entity.PackageStatusEntity
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CameraScreenApiService {

    @GET("/v1/config/demo")
    fun getCropScreenConfig(): Single<ApiResponse<ApiCropScreenConfig>>

    @GET("/v2/camera/get-settings")
    fun getCameraSettingConfig(
        @Query(value = "openCount") openCount: String,
        @Query("studentClass") studentClass: String,
        @Query("questionAskCount") questionAskCount: Long,
        @Query("has_camera_permission") hasCameraPermission: Boolean?
    ): Single<ApiResponse<CameraSettingEntity>>

    @GET("/v1/camera/get-animation")
    fun getDemoAnimationList(): Single<ApiResponse<List<DemoAnimationEntity>>>

    @POST("/v1/camera/post-face-data")
    fun saveSelfieDetectedImage(@Body params: RequestBody): Completable

    @GET("/v1/package/doubt/status")
    fun getPackageStatus(): Single<ApiResponse<PackageStatusEntity>>
}
