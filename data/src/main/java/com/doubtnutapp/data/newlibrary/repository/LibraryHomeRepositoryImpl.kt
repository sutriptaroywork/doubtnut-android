package com.doubtnutapp.data.newlibrary.repository

import com.doubtnutapp.data.newlibrary.mapper.LibraryHomeMapper
import com.doubtnutapp.data.newlibrary.service.LibraryHomeService
import com.doubtnutapp.domain.newlibrary.entities.LibraryDataEntity
import com.doubtnutapp.domain.newlibrary.repository.LibraryHomeRepository
import io.reactivex.Single
import javax.inject.Inject

class LibraryHomeRepositoryImpl @Inject constructor(
    private val libraryHomeService: LibraryHomeService,
    private val libraryHomeMapper: LibraryHomeMapper
) : LibraryHomeRepository {

    override fun getLibraryHomeData(studentClass: Int, featureIds: List<Int>): Single<List<LibraryDataEntity>> =
        libraryHomeService.getLibraryHomeData(studentClass, featureIds)
            .map {
                it.data.map { apiLibraryData ->
                    libraryHomeMapper.map(apiLibraryData)
                }
            }
}
