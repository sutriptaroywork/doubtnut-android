package com.doubtnutapp.data.gamification.gamificationbadges.userbadges.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.model.ApiUserBadge
import com.doubtnutapp.domain.gamification.userbadge.entity.BaseUserBadge
import com.doubtnutapp.domain.gamification.userbadge.entity.UserBadgeEntity
import com.doubtnutapp.domain.gamification.userbadge.entity.UserBadgeHeaderEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserBadgeListEntityMapper @Inject constructor() :
    Mapper<HashMap<String, List<ApiUserBadge>>, List<BaseUserBadge>> {
    override fun map(srcObject: HashMap<String, List<ApiUserBadge>>): List<BaseUserBadge> {
        val badgeList = mutableListOf<BaseUserBadge>()
        val keys = srcObject.keys
        for (key in keys) {

            badgeList.add(
                UserBadgeHeaderEntity(
                    title = key,
                    type = HEADER
                )
            )

            val list = srcObject[key]
            list?.forEach {
                badgeList.add(
                    UserBadgeEntity(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        nudgeDescription = it.nudgeDescription,
                        imageUrl = it.imageUrl,
                        isAchieved = it.isAchieved,
                        sharingMessage = it.sharingMessage,
                        actionPage = it.actionPage ?: "",
                        type = key,
                        blurImage = it.blurImage.orEmpty()
                    )
                )
            }
        }
        return badgeList
    }

    companion object {
        val HEADER = "header"
    }
}
