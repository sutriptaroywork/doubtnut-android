package com.doubtnutapp.domain.newlibrary.repository

import com.doubtnutapp.domain.newlibrary.entities.LibraryDataEntity
import io.reactivex.Single

interface LibraryHomeRepository {

    fun getLibraryHomeData(studentClass: Int, featureIds: List<Int>): Single<List<LibraryDataEntity>>
}
