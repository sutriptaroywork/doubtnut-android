package com.doubtnutapp.domain.gamification.mybio.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.mybio.entity.StudentClassEntity
import com.doubtnutapp.domain.gamification.mybio.repository.UserBioRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-11-22.
 */
class GetClassListUseCase @Inject constructor(
    private val userBioRepository: UserBioRepository
) : SingleUseCase<ArrayList<StudentClassEntity>, Unit> {

    override fun execute(param: Unit): Single<ArrayList<StudentClassEntity>> =
        userBioRepository.getClassList()
}
