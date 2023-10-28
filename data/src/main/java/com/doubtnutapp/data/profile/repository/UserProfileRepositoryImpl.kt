package com.doubtnutapp.data.profile.repository

import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.profile.mapper.UserProfileMapper
import com.doubtnutapp.data.profile.service.UserProfileService
import com.doubtnutapp.domain.profile.UserProfileEntity
import com.doubtnutapp.domain.profile.UserProfileRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileService: UserProfileService,
    private val userProfileMapper: UserProfileMapper,
    private val userPreference: UserPreference
) : UserProfileRepository {

    override fun getClass(): Completable {

        return userProfileService.getClassData(userPreference.getSelectedLanguage())
            .flatMapCompletable {
                Completable.fromCallable {
                    for (i in it.data.indices) {
                        if (userPreference.getUserClass() == it.data[i].className) userPreference.updateClass(
                            it.data[i].className,
                            it.data[i].classDisplay
                        )
                    }
                }
            }
    }

    override fun getProfile(): Single<UserProfileEntity> {

        return userProfileService.getProfileDetails(userPreference.getUserStudentId()).map {
            userProfileMapper.map(it.data)
        }
    }
}
