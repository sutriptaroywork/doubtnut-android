package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.SocialService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.feed.ApiFollowerWidgetItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SocialRepository(private val socialService: SocialService) {

    fun getUserBanStatus() = socialService.userBanStatus()

    fun getUserUnBanRequestStatus(userId: String) = socialService.getUserUnBanRequestStatus(userId)

    fun getUserFollowing(page: Int, userId: String) = socialService.getUserFollowing(userId, page)

    fun getUserFollowers(page: Int, userId: String) = socialService.getUserFollowers(userId, page)

    fun reportUser(userId: String) = socialService.reportUser(userId)

    fun userReportStatus(userId: String) = socialService.userReportStatus(userId)

    fun removeFollower(followerUserId: String) = socialService.removeFollower(followerUserId)

    fun sendUnbanRequest(userId: String) = socialService.sendUnbanRequest(userId)

    suspend fun getUsersToFollow(): Flow<ApiResponse<ApiFollowerWidgetItems>> {
        return flow {
            emit(socialService.getUsersToFollow())
        }
    }
}
