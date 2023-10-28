package com.doubtnutapp.gamification.badgesscreen.mapper

import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.mapper.UserBadgeListEntityMapper.Companion.HEADER
import com.doubtnutapp.domain.gamification.userbadge.entity.BaseUserBadge
import com.doubtnutapp.domain.gamification.userbadge.entity.UserBadgeEntity
import com.doubtnutapp.domain.gamification.userbadge.entity.UserBadgeHeaderEntity
import com.doubtnutapp.gamification.FEATURE_TYPE_BADGE
import com.doubtnutapp.gamification.badgesscreen.model.Badge
import com.doubtnutapp.gamification.badgesscreen.model.BadgeHeaderViewType
import com.doubtnutapp.gamification.badgesscreen.model.BaseBadgeViewType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeMapper @Inject constructor() : Mapper<BaseUserBadge, BaseBadgeViewType> {

    override fun map(srcObject: BaseUserBadge): BaseBadgeViewType = with(srcObject) {
        when (type) {
            HEADER -> {
                val title = (this as UserBadgeHeaderEntity).title.replace("_", " ").capitalize()
                BadgeHeaderViewType(
                        title = title,
                        viewType = R.layout.item_badge_header
                )
            }
            else -> {
                val userBadgeEntity = this as UserBadgeEntity
                Badge(
                        featureType = FEATURE_TYPE_BADGE,
                        id = userBadgeEntity.id,
                        name = userBadgeEntity.name,
                        description = userBadgeEntity.description,
                        nudgeDescription = userBadgeEntity.nudgeDescription,
                        imageUrl = userBadgeEntity.imageUrl,
                        isAchieved = (userBadgeEntity.isAchieved == 1),
                        sharingMessage = userBadgeEntity.sharingMessage,
                        actionPage = userBadgeEntity.actionPage,
                        isOwn = false,
                        blurImage = userBadgeEntity.blurImage,
                        viewType = R.layout.item_badge
                )
            }
        }
    }

}