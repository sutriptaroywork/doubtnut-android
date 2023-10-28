package com.doubtnutapp.data.gamification.gamificationbadges.userbadges.repository

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.model.ApiBadgeProgress
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.model.ApiUserBadge
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface UserBadgeService {

    @GET("/v2/gamification/{userId}/badge/all")
    fun getBadges(@Path("userId") userId: String): Single<ApiResponse<HashMap<String, List<ApiUserBadge>>>>

    @GET("/v2/gamification/{badgeId}/progress")
    fun getBadgeProgress(@Path("badgeId") badgeId: String): Single<ApiResponse<ApiBadgeProgress>>
}
