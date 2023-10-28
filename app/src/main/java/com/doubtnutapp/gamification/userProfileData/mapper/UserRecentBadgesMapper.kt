package com.doubtnutapp.gamification.userProfileData.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.userProfile.entity.MyBadgesItemEntity
import com.doubtnutapp.gamification.userProfileData.model.MyBadgesItemDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRecentBadgesMapper @Inject constructor() :
    Mapper<MyBadgesItemEntity, MyBadgesItemDataModel> {
    override fun map(srcObject: MyBadgesItemEntity) = with(srcObject) {
        MyBadgesItemDataModel(
            if (isAchieved) {
                imageUrl
            } else {
                blurImage
            },
            name,
            description,
            recentBadgesId,
            isAchieved
        )
    }

}