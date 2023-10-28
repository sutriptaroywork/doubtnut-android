package com.doubtnutapp.data.gamification.dailyattendance.repository

import com.doubtnutapp.data.gamification.dailyattendance.mapper.DailyAttendanceEntityMapper
import com.doubtnutapp.data.gamification.dailyattendance.service.DailyAttendanceService
import com.doubtnutapp.domain.gamification.dailyattendance.entity.DailyAttendanceEntity
import com.doubtnutapp.domain.gamification.dailyattendance.repository.DailyAttendanceRepository
import io.reactivex.Single
import javax.inject.Inject

class DailyAttendanceRepositoryImpl @Inject constructor(
    private val dailyAttendanceService: DailyAttendanceService,
    private val dailyAttendanceEntityMapper: DailyAttendanceEntityMapper
) : DailyAttendanceRepository {

    override fun getDailyAttendance(userId: String): Single<DailyAttendanceEntity> =
        dailyAttendanceService.getDailyAttendance(userId)
            .map {
                dailyAttendanceEntityMapper.map(it.data)
            }
}
