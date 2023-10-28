package com.doubtnutapp.camera.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.camerascreen.entity.PackageStatusEntity
import com.doubtnutapp.domain.camerascreen.repository.CameraScreenRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-19.
 */
class GetPackageStatusUseCase @Inject constructor(
    private val cameraScreenRepository: CameraScreenRepository
) : SingleUseCase<PackageStatusEntity, Unit> {

    override fun execute(param: Unit): Single<PackageStatusEntity> =
        cameraScreenRepository.getPackageStatus()
}
