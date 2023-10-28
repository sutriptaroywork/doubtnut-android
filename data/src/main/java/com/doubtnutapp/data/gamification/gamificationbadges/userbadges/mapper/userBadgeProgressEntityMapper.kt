package com.doubtnutapp.data.gamification.gamificationbadges.userbadges.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.model.ApiBadgeProgress
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.model.ApiRequirementsItem
import com.doubtnutapp.domain.gamification.userbadge.entity.BadgeProgressEntity
import com.doubtnutapp.domain.gamification.userbadge.entity.RequirementsItemEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class userBadgeProgressEntityMapper @Inject constructor() :
    Mapper<ApiBadgeProgress, BadgeProgressEntity> {
    override fun map(srcObject: ApiBadgeProgress) = with(srcObject) {
        BadgeProgressEntity(
            getRequirementsData(requirements),
            nudgeDescription,
            imageUrl,
            name,
            description
        )
    }

    private fun getRequirementsData(requirements: List<ApiRequirementsItem>?): List<RequirementsItemEntity>? =
        requirements?.map {

            RequirementsItemEntity(
                it.requirementType,
                it.fullfilled,
                it.fullfilledPercent,
                it.requirement
            )
        }
}
