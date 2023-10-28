package com.doubtnutapp.domain.liveclasseslibrary.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.liveclasseslibrary.repository.LiveClassesRepository
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestDetailsEntity
import io.reactivex.Single
import javax.inject.Inject

class GetMockDataUseCase @Inject constructor(private val liveClassesRepository: LiveClassesRepository) :
    SingleUseCase<MockTestDetailsEntity, GetMockDataUseCase.Param> {

    override fun execute(param: Param): Single<MockTestDetailsEntity> =
        liveClassesRepository.getLiveClassesMockData(param.mockTestId)

    @Keep
    class Param(val mockTestId: Int)
}
