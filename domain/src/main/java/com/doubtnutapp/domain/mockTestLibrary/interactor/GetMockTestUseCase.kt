package com.doubtnutapp.domain.mockTestLibrary.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestEntity
import com.doubtnutapp.domain.mockTestLibrary.repository.MockTestRepository
import io.reactivex.Single
import javax.inject.Inject

class GetMockTestUseCase @Inject constructor(private val mockTestRepository: MockTestRepository) :
    SingleUseCase<List<MockTestEntity>, GetMockTestUseCase.None> {

    override fun execute(param: None): Single<List<MockTestEntity>> = mockTestRepository.getMockTestData()

    class None
}
