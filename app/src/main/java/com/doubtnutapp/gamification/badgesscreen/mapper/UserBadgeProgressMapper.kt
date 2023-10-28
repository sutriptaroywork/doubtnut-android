package com.doubtnutapp.gamification.badgesscreen.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.userbadge.entity.BadgeProgressEntity
import com.doubtnutapp.domain.gamification.userbadge.entity.RequirementsItemEntity
import com.doubtnutapp.gamification.badgesscreen.model.BadgeProgress
import com.doubtnutapp.gamification.badgesscreen.model.RequirementsItemDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserBadgeProgressMapper @Inject constructor() : Mapper<BadgeProgressEntity, BadgeProgress> {
    override fun map(srcObject: BadgeProgressEntity) = with(srcObject) {
        BadgeProgress(
                getRequirementsData(requirements),
                nudgeDescription,
                imageUrl,
                name,
                description
        )
    }

    private fun getRequirementsData(requirements: List<RequirementsItemEntity>?): List<RequirementsItemDataModel>? = requirements?.map {

        RequirementsItemDataModel(
                it.requirementType,
                it.fullfilled,
                it.fullfilledPercent,
                it.requirement
        )
    }
}