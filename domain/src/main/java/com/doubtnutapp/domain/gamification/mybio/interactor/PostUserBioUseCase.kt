package com.doubtnutapp.domain.gamification.mybio.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.mybio.repository.UserBioRepository
import com.google.gson.JsonObject
import io.reactivex.Single
import javax.inject.Inject

class PostUserBioUseCase @Inject constructor(
    private val userBioRepository: UserBioRepository
) : SingleUseCase<String, PostUserBioUseCase.Param> {

    override fun execute(param: Param): Single<String> =
        userBioRepository.postUserBio(userBio = param.userData)

    @Keep
    data class Param(
        val userData: JsonObject
    )
}
