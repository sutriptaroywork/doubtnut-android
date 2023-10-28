package com.doubtnutapp.domain.homefeed.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.homefeed.repository.HomeScreenRepository
import com.doubtnutapp.domain.library.entities.ClassListEntity
import io.reactivex.Single
import javax.inject.Inject

class GetClassesListHome @Inject constructor(private val homeScreenRepository: HomeScreenRepository) :
    SingleUseCase<ClassListEntity, Unit> {

    override fun execute(param: Unit): Single<ClassListEntity> =
        homeScreenRepository.getClassesList()
}
