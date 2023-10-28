package com.doubtnutapp.data.gamification.dailyattendance.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.dailyattendance.model.ApiDailyAttendance
import com.doubtnutapp.domain.gamification.dailyattendance.entity.DailyAttendanceEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Sachin Kumar on 28/6/19.
 */
@Singleton
class DailyAttendanceEntityMapper @Inject constructor() : Mapper<ApiDailyAttendance, DailyAttendanceEntity> {

    override fun map(srcObject: ApiDailyAttendance): DailyAttendanceEntity = with(srcObject) {
        DailyAttendanceEntity(
            title.orEmpty(),
            titleImg.orEmpty(),
            heading.orEmpty(),
            dailyStreak?.let {
                it.map {
                    mapAttendanceStreak(it)
                }
            } ?: emptyList(),
            longestStreak ?: 0,
            longestStreakImage.orEmpty()
        )
    }

    private fun mapAttendanceStreak(apiCurrentStreak: ApiDailyAttendance.ApiCurrentStreak) = with(apiCurrentStreak) {
        DailyAttendanceEntity.CurrentStreakEntity(
            title.orEmpty(),
            icon.orEmpty(),
            isAchieved ?: 0,
            type.orEmpty(),
            points ?: 0
        )
    }
}
