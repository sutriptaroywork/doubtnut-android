package com.doubtnutapp.data.gamification.gamificationbadges.userProfile.repository

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.gamification.userProfile.model.ApiGamificationUserProfile
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface UserProfileService {

    @GET("v3/gamification/{student_id}/profile")
    fun getUserProfile(@Path("student_id") student_id: String): Single<ApiResponse<ApiGamificationUserProfile>>
}
