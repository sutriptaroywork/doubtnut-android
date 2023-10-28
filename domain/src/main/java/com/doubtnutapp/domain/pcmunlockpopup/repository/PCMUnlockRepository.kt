package com.doubtnutapp.domain.pcmunlockpopup.repository

import com.doubtnutapp.domain.pcmunlockpopup.entity.PCMUnlockDataEntity
import io.reactivex.Single

interface PCMUnlockRepository {
    fun getPCMUnlockData(): Single<PCMUnlockDataEntity>
}
