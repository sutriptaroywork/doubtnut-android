package com.doubtnutapp.domain.gamification.userProfile.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.userProfile.model.UserProfileEntity
import com.doubtnutapp.domain.gamification.userProfile.repository.UserProfileRepository
import io.reactivex.Single
import javax.inject.Inject

class GetUserProfile @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : SingleUseCase<UserProfileEntity, Unit> {

    override fun execute(param: Unit): Single<UserProfileEntity> {
        return userProfileRepository.getUserProfile()
    }
}
