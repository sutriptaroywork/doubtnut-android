package com.doubtnutapp.domain.gamification.dailyattendance.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.dailyattendance.entity.DailyAttendanceEntity
import com.doubtnutapp.domain.gamification.dailyattendance.repository.DailyAttendanceRepository
import io.reactivex.Single
import javax.inject.Inject

data class GetDailyAttendance @Inject constructor (
    private val dailyAttendanceRepository: DailyAttendanceRepository
) : SingleUseCase<DailyAttendanceEntity, GetDailyAttendance.Param> {

    override fun execute(param: Param): Single<DailyAttendanceEntity> =
        dailyAttendanceRepository.getDailyAttendance(param.userId)

    @Keep
    class Param(val userId: String)
}
