package com.doubtnutapp.camera.interactor

import com.doubtnutapp.camera.service.CropQuestionRepository
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.camerascreen.entity.CropScreenConfigEntity
import io.reactivex.Single
import javax.inject.Inject

class GetCropScreenConfigValue @Inject constructor(
    private val cropScreenRepository: CropQuestionRepository
) : SingleUseCase<CropScreenConfigEntity, Unit> {

    override fun execute(param: Unit): Single<CropScreenConfigEntity> =
        cropScreenRepository.getCropScreenConfig()
}
