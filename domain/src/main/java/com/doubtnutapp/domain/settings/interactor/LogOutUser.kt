package com.doubtnutapp.domain.settings.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.settings.repository.SettingRepository
import javax.inject.Inject

class LogOutUser @Inject constructor(
    private val profileSettingRepository: SettingRepository
) : CompletableUseCase<Unit> {

    override fun execute(param: Unit) = profileSettingRepository.logOutUser()
}
