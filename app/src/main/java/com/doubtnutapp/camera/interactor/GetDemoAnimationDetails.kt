package com.doubtnutapp.camera.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.camerascreen.entity.DemoAnimationEntity
import com.doubtnutapp.domain.camerascreen.repository.CameraScreenRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2020-01-07.
 */
class GetDemoAnimationDetails @Inject constructor(
    private val cameraScreenRepository: CameraScreenRepository
) : SingleUseCase<List<DemoAnimationEntity>, Unit> {

    override fun execute(param: Unit): Single<List<DemoAnimationEntity>> =
        cameraScreenRepository.getDemoAnimationList()
}
