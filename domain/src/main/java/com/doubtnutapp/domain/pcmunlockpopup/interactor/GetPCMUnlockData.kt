package com.doubtnutapp.domain.pcmunlockpopup.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.pcmunlockpopup.entity.PCMUnlockDataEntity
import com.doubtnutapp.domain.pcmunlockpopup.repository.PCMUnlockRepository
import io.reactivex.Single
import javax.inject.Inject

class GetPCMUnlockData @Inject constructor(
    private val pcmUnlockRepository: PCMUnlockRepository
) : SingleUseCase<PCMUnlockDataEntity, Unit> {
    override fun execute(param: Unit): Single<PCMUnlockDataEntity> = pcmUnlockRepository.getPCMUnlockData()
}
