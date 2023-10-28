package com.doubtnutapp.camera.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.camerascreen.repository.CameraScreenRepository
import io.reactivex.Single
import javax.inject.Inject

class CheckTrickyQuestionButtonShown @Inject constructor(private val cameraScreenRepository: CameraScreenRepository) :
    SingleUseCase<Boolean, Unit> {

    override fun execute(param: Unit): Single<Boolean> =
        cameraScreenRepository.isTrickyQuestionButtonShown()
}
