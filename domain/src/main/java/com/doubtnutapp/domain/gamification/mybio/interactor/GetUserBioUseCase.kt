package com.doubtnutapp.domain.gamification.mybio.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.mybio.entity.ApiUserBio
import com.doubtnutapp.domain.gamification.mybio.repository.UserBioRepository
import io.reactivex.Single
import javax.inject.Inject

class GetUserBioUseCase @Inject constructor(
    private val userBioRepository: UserBioRepository
) : SingleUseCase<ApiUserBio, Unit> {

    override fun execute(param: Unit): Single<ApiUserBio> =
        userBioRepository.getUserBio()
}
