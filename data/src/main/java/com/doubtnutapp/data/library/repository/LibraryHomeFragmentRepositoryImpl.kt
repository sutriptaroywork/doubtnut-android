package com.doubtnutapp.data.library.repository

import com.doubtnutapp.data.library.mapper.ClassEntityMapper
import com.doubtnutapp.data.library.service.LibraryHomeFragmentService
import com.doubtnutapp.domain.library.entities.ClassListEntity
import com.doubtnutapp.domain.library.repository.LibraryHomeFragmentRepository
import io.reactivex.Single
import javax.inject.Inject

class LibraryHomeFragmentRepositoryImpl @Inject constructor(
    private val libraryHomeFragmentService: LibraryHomeFragmentService,
    private val classMapper: ClassEntityMapper
) : LibraryHomeFragmentRepository {

    override fun getClassesList(): Single<ClassListEntity> =
        libraryHomeFragmentService.getClassesList()
            .map {
                classMapper.map(it.data)
            }
}
