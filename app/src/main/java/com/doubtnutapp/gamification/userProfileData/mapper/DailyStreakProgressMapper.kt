package com.doubtnutapp.gamification.userProfileData.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.userProfile.entity.DailyAttendanceEntity
import com.doubtnutapp.gamification.userProfileData.model.DailyAttendanceDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyStreakProgressMapper @Inject constructor() : Mapper<DailyAttendanceEntity, DailyAttendanceDataModel> {

    override fun map(srcObject: DailyAttendanceEntity) = with(srcObject) {
        DailyAttendanceDataModel(
            isAchieved == IS_ACHIEVED,
            itemIcon,
            itemTitle,
            badgeType,
            points
        )
    }

    companion object {
        private const val IS_ACHIEVED = 1
    }

}