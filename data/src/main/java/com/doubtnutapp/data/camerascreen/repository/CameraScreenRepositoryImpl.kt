package com.doubtnutapp.data.camerascreen.repository

import com.doubtnutapp.data.base.di.qualifier.ApplicationCachePath
import com.doubtnutapp.data.base.di.qualifier.CropDefaultImagePath
import com.doubtnutapp.data.camerascreen.apiService.CameraScreenApiService
import com.doubtnutapp.data.camerascreen.datasource.ConfigDataSources
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.camerascreen.entity.CameraConfigEntity
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.domain.camerascreen.entity.DemoAnimationEntity
import com.doubtnutapp.domain.camerascreen.entity.PackageStatusEntity
import com.doubtnutapp.domain.camerascreen.repository.CameraScreenRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class CameraScreenRepositoryImpl @Inject constructor(
    private val configDataSources: ConfigDataSources,
    private val cameraScreenApiService: CameraScreenApiService,
    private val userPreference: UserPreference,
    @ApplicationCachePath private val appCachePath: String,
    @CropDefaultImagePath private val defaultImagePath: String
) : CameraScreenRepository {

    override fun getCameraScreenConfig(): Single<CameraConfigEntity> = Single.fromCallable {
        configDataSources.getCameraScreenConfig()
    }

    override fun isTrickyQuestionButtonShown(): Single<Boolean> = Single.fromCallable {
        userPreference.isTrickyQuestionButtonShown()
    }

    override fun setCameraScreenShownFirstToTrue() = Completable.fromCallable {
        userPreference.setIsCameraScreenShownToTrue()
    }

    override fun setTrickyQuestionShownToTrue() = Completable.fromCallable {
        userPreference.setIsTrickyQuestionButtonToTrue()
    }

    override fun getCameraSettingConfig(hasCameraPermission: Boolean): Single<CameraSettingEntity> {
        return cameraScreenApiService.getCameraSettingConfig(
            userPreference.getCameraScreenVisitCount().toString(),
            userPreference.getUserClass(),
            userPreference.getQuestionAskCount(),
            hasCameraPermission
        ).map {
            it.data
        }
    }

    override fun getDemoAnimationList(): Single<List<DemoAnimationEntity>> {
        return cameraScreenApiService.getDemoAnimationList().map {
            it.data
        }
    }

    override fun saveSelfieDetectedImage(imageInBase64: String): Completable {
        val params = hashMapOf(
            "face_img" to imageInBase64
        ).toRequestBody()
        return cameraScreenApiService.saveSelfieDetectedImage(params)
    }

    override fun getPackageStatus(): Single<PackageStatusEntity> {
        return cameraScreenApiService.getPackageStatus().map {
            it.data
        }
    }
}
