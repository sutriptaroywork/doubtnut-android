package com.doubtnutapp.domain.gamification.userProfile.repository

import com.doubtnutapp.domain.gamification.userProfile.model.UserProfileEntity
import io.reactivex.Single

interface UserProfileRepository {
    fun getUserProfile(): Single<UserProfileEntity>
    fun getOthersUserProfile(userId: String): Single<UserProfileEntity>
}
