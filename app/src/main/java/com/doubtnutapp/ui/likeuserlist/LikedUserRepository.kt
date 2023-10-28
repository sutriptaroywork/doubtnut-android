package com.doubtnutapp.ui.likeuserlist

class LikedUserRepository(private val likedUserService: LikedUserService) {
    fun getUsersList(token: String, feedId: String, feedType: String) =
        likedUserService.getUserList(token, feedId, feedType)
}