package com.doubtnutapp.data.gamification.mybio.repository

import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.gamification.mybio.service.UserBioService
import com.doubtnutapp.domain.gamification.mybio.entity.ApiUserBio
import com.doubtnutapp.domain.gamification.mybio.entity.StudentClassEntity
import com.doubtnutapp.domain.gamification.mybio.repository.UserBioRepository
import com.google.gson.JsonObject
import io.reactivex.Single
import javax.inject.Inject

class UserBioRepositoryImpl @Inject constructor(
    private val userBioService: UserBioService,
    private val userPreference: UserPreference
) : UserBioRepository {
    override fun getClassList(): Single<ArrayList<StudentClassEntity>> {
        return userBioService.getClassesWithSSC(userPreference.getSelectedLanguage()).map {
            it.data
        }
    }

    override fun postUserBio(userBio: JsonObject): Single<String> =
        userBioService.postUserBio(userPreference.getUserStudentId(), userBio).map {
            it.meta.message
        }

    override fun getUserBio(): Single<ApiUserBio> =
        userBioService.getUserBio(userPreference.getUserStudentId()).map {
            it.data
        }
}
