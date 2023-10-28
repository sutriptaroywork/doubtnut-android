package com.doubtnutapp.domain.gamification.userProfile.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.userProfile.model.UserProfileEntity
import com.doubtnutapp.domain.gamification.userProfile.repository.UserProfileRepository
import io.reactivex.Single
import javax.inject.Inject

class GetOthersUserProfile @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : SingleUseCase<UserProfileEntity, GetOthersUserProfile.Param> {

    override fun execute(param: Param): Single<UserProfileEntity> = userProfileRepository.getOthersUserProfile(param.userId)

    @Keep
    class Param(val userId: String)
}
