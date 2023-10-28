package com.doubtnutapp.domain.mockTestLibrary.repository

import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestEntity
import io.reactivex.Single

interface MockTestRepository {

    fun getMockTestData(): Single<List<MockTestEntity>>
}
