package com.doubtnutapp.data.gamification.userProfile.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.userProfile.model.ApiMyBadgesItem
import com.doubtnutapp.domain.gamification.userProfile.entity.MyBadgesItemEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyBadgesEntityMapper @Inject constructor() : Mapper<ApiMyBadgesItem, MyBadgesItemEntity> {
    private val BADGE_ACHIEVED = 1
    override fun map(srcObject: ApiMyBadgesItem) = with(srcObject) {
        MyBadgesItemEntity(
            imageUrl.orEmpty(),
            name.orEmpty(),
            description.orEmpty(),
            badgesId ?: 0,
            isAchieved == BADGE_ACHIEVED,
            blurImage.orEmpty()
        )
    }
}
