package com.doubtnutapp.domain.gamification.dailyattendance.repository

import com.doubtnutapp.domain.gamification.dailyattendance.entity.DailyAttendanceEntity
import io.reactivex.Single

/**
 * Created by Sachin Kumar on 28/6/19.
 */
interface DailyAttendanceRepository {
    fun getDailyAttendance(userId: String): Single<DailyAttendanceEntity>
}
