package com.doubtnutapp.domain.gamification.mybio.repository

import com.doubtnutapp.domain.gamification.mybio.entity.ApiUserBio
import com.doubtnutapp.domain.gamification.mybio.entity.StudentClassEntity
import com.google.gson.JsonObject
import io.reactivex.Single

interface UserBioRepository {

    fun getUserBio(): Single<ApiUserBio>

    fun postUserBio(userBio: JsonObject): Single<String>

    fun getClassList(): Single<ArrayList<StudentClassEntity>>
}
