package com.doubtnutapp.data.mocktestLibrary.repository

import com.doubtnutapp.data.mocktestLibrary.mapper.MockTestEntityMapper
import com.doubtnutapp.data.mocktestLibrary.service.MockTestService
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestEntity
import com.doubtnutapp.domain.mockTestLibrary.repository.MockTestRepository
import io.reactivex.Single
import javax.inject.Inject

class MockTestRepositoryImpl @Inject constructor(
    private val mockTestService: MockTestService,
    private val mockTestEntityMapper: MockTestEntityMapper
) : MockTestRepository {

    override fun getMockTestData(): Single<List<MockTestEntity>> =
        mockTestService
            .getMockTestData()
            .map { response ->
                response.data.map {
                    mockTestEntityMapper.map(it)
                }
            }
}
