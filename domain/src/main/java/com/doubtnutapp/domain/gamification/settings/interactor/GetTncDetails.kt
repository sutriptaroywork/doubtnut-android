package com.doubtnutapp.domain.gamification.settings.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.settings.entity.SettingDetailEntity
import com.doubtnutapp.domain.gamification.settings.repository.SettingDetailsRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by shrreya on 3/7/19.
 */
class GetTncDetails @Inject constructor(
    private val settingDetailsRepository: SettingDetailsRepository
) : SingleUseCase<SettingDetailEntity, Unit> {

    override fun execute(param: Unit): Single<SettingDetailEntity> {
        return settingDetailsRepository.getTncDetails()
    }
}
