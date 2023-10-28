package com.doubtnutapp.data.pcmunlockpopup.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.pcmunlockpopup.model.ApiBadgeRequired
import com.doubtnutapp.data.pcmunlockpopup.model.ApiPCMUnlockData
import com.doubtnutapp.domain.pcmunlockpopup.entity.BadgeRequired
import com.doubtnutapp.domain.pcmunlockpopup.entity.PCMUnlockDataEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PCMUnlockDataEntityMapper @Inject constructor() :
    Mapper<ApiPCMUnlockData, PCMUnlockDataEntity> {
    override fun map(srcObject: ApiPCMUnlockData): PCMUnlockDataEntity {
        return with(srcObject) {
            PCMUnlockDataEntity(
                heading,
                subheading,
                getRequiredBadgeList(apiBadgeRequired),
                userImages,
                footerText
            )
        }
    }

    private fun getRequiredBadgeList(apiBadgeRequired: List<ApiBadgeRequired>): List<BadgeRequired> {
        return apiBadgeRequired.map {
            BadgeRequired(
                it.currentProgress,
                it.description,
                it.id,
                it.imageUrl,
                it.name,
                it.nudgeDescription,
                it.requirement,
                it.requirementType
            )
        }
    }
}
