package com.doubtnutapp.domain.profile

import com.doubtnutapp.domain.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(private val userProfileRepository: UserProfileRepository) :
    SingleUseCase<UserProfileEntity, GetUserProfileUseCase.None> {

    override fun execute(param: None): Single<UserProfileEntity> =
        userProfileRepository.getProfile()

    class None
}
