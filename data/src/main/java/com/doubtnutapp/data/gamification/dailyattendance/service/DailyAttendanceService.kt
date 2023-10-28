package com.doubtnutapp.data.gamification.dailyattendance.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.gamification.dailyattendance.model.ApiDailyAttendance
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface DailyAttendanceService {

    @GET("v2/gamification/{student_id}/badge/dailystreak")
    fun getDailyAttendance(@Path("student_id") student_id: String): Single<ApiResponse<ApiDailyAttendance>>
}
