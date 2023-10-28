package com.doubtnutapp.domain.gamification.userbadge.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.userbadge.entity.BadgeProgressEntity
import com.doubtnutapp.domain.gamification.userbadge.repository.UserBadgesRepository
import io.reactivex.Single
import javax.inject.Inject

class GetUserBadgeProgress @Inject constructor(
    private val userBadgeRepository: UserBadgesRepository
) : SingleUseCase<BadgeProgressEntity, GetUserBadgeProgress.Param> {

    override fun execute(param: GetUserBadgeProgress.Param): Single<BadgeProgressEntity> {
        return userBadgeRepository.getUserBadgeProgress(param.badgeId)
    }

    @Keep
    class Param(val badgeId: String)
}
