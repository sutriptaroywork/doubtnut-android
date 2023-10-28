package com.doubtnutapp.domain.library.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.library.entities.ClassListEntity
import com.doubtnutapp.domain.library.repository.LibraryHomeFragmentRepository
import io.reactivex.Single
import javax.inject.Inject

class GetClassesList @Inject constructor(private val libraryHomeFragmentRepository: LibraryHomeFragmentRepository) :
    SingleUseCase<ClassListEntity, Unit> {

    override fun execute(param: Unit): Single<ClassListEntity> =
        libraryHomeFragmentRepository.getClassesList()
}
