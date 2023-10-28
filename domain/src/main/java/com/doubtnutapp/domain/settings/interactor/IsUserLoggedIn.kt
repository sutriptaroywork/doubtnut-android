package com.doubtnutapp.domain.settings.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.settings.repository.SettingRepository
import io.reactivex.Single
import javax.inject.Inject

class IsUserLoggedIn @Inject constructor(
    private val settingRepository: SettingRepository
) : SingleUseCase<Boolean, Unit> {

    override fun execute(param: Unit): Single<Boolean> {
        return settingRepository.getUserLoggedInState()
    }
}
