package com.doubtnutapp.domain.gamification.userbadge.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.userbadge.entity.BaseUserBadge
import com.doubtnutapp.domain.gamification.userbadge.repository.UserBadgesRepository
import io.reactivex.Single
import javax.inject.Inject

class GetUserBadges @Inject constructor(
    private val userBadgeRepository: UserBadgesRepository
) : SingleUseCase<List<BaseUserBadge>, GetUserBadges.Param> {

    override fun execute(param: Param): Single<List<BaseUserBadge>> {
        return userBadgeRepository.getUserBadgesList(param.userId)
    }

    @Keep
    class Param(val userId: String)
}
