package com.doubtnutapp.camera.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.camerascreen.entity.CameraConfigEntity
import com.doubtnutapp.domain.camerascreen.repository.CameraScreenRepository
import io.reactivex.Single
import javax.inject.Inject

class GetCameraConfigValue @Inject constructor(
    private val cameraScreenRepository: CameraScreenRepository
) : SingleUseCase<CameraConfigEntity, Unit> {

    override fun execute(param: Unit): Single<CameraConfigEntity> =
        cameraScreenRepository.getCameraScreenConfig()
}
