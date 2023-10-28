package com.doubtnutapp.domain.liveclasseslibrary.repository

import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestDetailsEntity
import io.reactivex.Single

interface LiveClassesRepository {

    fun getLiveClassesMockData(mockTestId: Int): Single<MockTestDetailsEntity>
}
