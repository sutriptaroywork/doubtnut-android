package com.doubtnutapp.data.gamification.gamificationbadges.userProfile.repository

import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.gamification.userProfile.mapper.UserProfileEntityMapper
import com.doubtnutapp.domain.gamification.userProfile.model.UserProfileEntity
import com.doubtnutapp.domain.gamification.userProfile.repository.UserProfileRepository
import io.reactivex.Single
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val userBadgeService: UserProfileService,
    private val userProfileEntityMapper: UserProfileEntityMapper,
    private val userPreference: UserPreference
) : UserProfileRepository {

    override fun getUserProfile(): Single<UserProfileEntity> {

        val userId = userPreference.getUserStudentId()

        return userBadgeService.getUserProfile(userId).map {
            userProfileEntityMapper.map(it.data)
        }.map {
            val isLogedIn = userPreference.getUserLoggedIn()
            userPreference.updateUserProfileData(
                it.userName.orEmpty(), it.userEmail ?: "",
                it.userSchoolName
                    ?: "",
                it.userPincode ?: "", it.userCoaching ?: "",
                it.userDob
                    ?: "",
                it.profileImage
            )
            it.copy(isLoggedIn = isLogedIn)
        }
    }

    override fun getOthersUserProfile(userId: String): Single<UserProfileEntity> {

        return userBadgeService.getUserProfile(userId).map {
            userProfileEntityMapper.map(it.data)
        }.map {
            val isLogedIn = userPreference.getUserLoggedIn()
            it.copy(isLoggedIn = isLogedIn)
        }
    }
}
