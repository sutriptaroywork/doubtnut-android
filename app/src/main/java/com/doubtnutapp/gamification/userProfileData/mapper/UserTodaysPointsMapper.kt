package com.doubtnutapp.gamification.userProfileData.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.userProfile.model.UserTodaysPointItemEntity
import com.doubtnutapp.gamification.userProfileData.model.UserTodaysPointItemDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserTodaysPointsMapper @Inject constructor() :
    Mapper<UserTodaysPointItemEntity, UserTodaysPointItemDataModel> {
    override fun map(srcObject: UserTodaysPointItemEntity) = with(srcObject) {
        UserTodaysPointItemDataModel(
            dailyPoint
        )
    }

}