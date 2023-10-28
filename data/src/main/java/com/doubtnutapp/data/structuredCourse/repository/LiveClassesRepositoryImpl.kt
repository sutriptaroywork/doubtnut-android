package com.doubtnutapp.data.structuredCourse.repository

import com.doubtnutapp.data.structuredCourse.mapper.MockTestMapper
import com.doubtnutapp.data.structuredCourse.service.LiveClassesService
import com.doubtnutapp.domain.liveclasseslibrary.repository.LiveClassesRepository
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestDetailsEntity
import io.reactivex.Single
import javax.inject.Inject

class LiveClassesRepositoryImpl @Inject constructor(
    private val liveClassesService: LiveClassesService,
    private val mockTestMapper: MockTestMapper
) : LiveClassesRepository {

    override fun getLiveClassesMockData(mockTestId: Int): Single<MockTestDetailsEntity> =
        liveClassesService.getLiveClassesMockData(mockTestId)
            .map {
                mockTestMapper.map(it.data)
            }
}
