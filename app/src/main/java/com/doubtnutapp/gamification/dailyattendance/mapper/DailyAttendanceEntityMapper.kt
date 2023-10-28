package com.doubtnutapp.gamification.dailyattendance.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.dailyattendance.entity.DailyAttendanceEntity
import com.doubtnutapp.gamification.dailyattendance.model.DailyAttendanceDataModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyAttendanceEntityMapper @Inject constructor(): Mapper<DailyAttendanceEntity, DailyAttendanceDataModel> {

    override fun map(srcObject: DailyAttendanceEntity): DailyAttendanceDataModel = with(srcObject) {
        DailyAttendanceDataModel(
                title,
                titleImg,
                heading,
                dailyStreakEntity.map {
                    DailyAttendanceDataModel.CurrentStreakDataModel(
                            title = it.title,
                            icon = it.icon,
                            isAchieved = it.isAchieved,
                            type = it.type,
                            points = it.points
                    )
                },
                longestStreak,
                longestStreakImage
        )
    }
}