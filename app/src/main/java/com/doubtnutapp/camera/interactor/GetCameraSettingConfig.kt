package com.doubtnutapp.camera.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.domain.camerascreen.repository.CameraScreenRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2020-01-07.
 */
class GetCameraSettingConfig @Inject constructor(
    private val cameraScreenRepository: CameraScreenRepository
) : SingleUseCase<CameraSettingEntity, GetCameraSettingConfig.Param> {

    override fun execute(param: Param): Single<CameraSettingEntity> =
        cameraScreenRepository.getCameraSettingConfig(param.hasCameraPermission)

    @Keep
    data class Param(
        val hasCameraPermission: Boolean
    )
}
