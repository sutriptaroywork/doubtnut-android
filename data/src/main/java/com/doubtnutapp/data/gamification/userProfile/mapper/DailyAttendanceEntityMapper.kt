package com.doubtnutapp.data.gamification.userProfile.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.userProfile.model.ApiDailyAttendanceItem
import com.doubtnutapp.domain.gamification.userProfile.entity.DailyAttendanceEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyAttendanceEntityMapper @Inject constructor() : Mapper<ApiDailyAttendanceItem, DailyAttendanceEntity> {
    override fun map(srcObject: ApiDailyAttendanceItem) = with(srcObject) {
        DailyAttendanceEntity(
            isAchieved,
            itemIcon,
            itemTitle,
            badgeType,
            points
        )
    }
}
