package com.doubtnutapp.camera.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.camerascreen.repository.CameraScreenRepository
import io.reactivex.Completable
import javax.inject.Inject

class SetIsCameraScreenShownFirstToTrue @Inject constructor(private val cameraScreenRepository: CameraScreenRepository) :
    CompletableUseCase<Unit> {

    override fun execute(param: Unit): Completable =
        cameraScreenRepository.setCameraScreenShownFirstToTrue()
}
