package com.doubtnutapp.domain.camerascreen.repository

import com.doubtnutapp.domain.camerascreen.entity.CameraConfigEntity
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.domain.camerascreen.entity.DemoAnimationEntity
import com.doubtnutapp.domain.camerascreen.entity.PackageStatusEntity
import io.reactivex.Completable
import io.reactivex.Single

interface CameraScreenRepository {

    fun getCameraScreenConfig(): Single<CameraConfigEntity>

    fun isTrickyQuestionButtonShown(): Single<Boolean>

    fun setCameraScreenShownFirstToTrue(): Completable

    fun setTrickyQuestionShownToTrue(): Completable

    fun getCameraSettingConfig(hasCameraPermission: Boolean): Single<CameraSettingEntity>

    fun getDemoAnimationList(): Single<List<DemoAnimationEntity>>

    fun saveSelfieDetectedImage(imageInBase64: String): Completable

    fun getPackageStatus(): Single<PackageStatusEntity>
}
