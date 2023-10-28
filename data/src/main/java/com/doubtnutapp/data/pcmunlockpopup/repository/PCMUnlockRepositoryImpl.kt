package com.doubtnutapp.data.pcmunlockpopup.repository

import com.doubtnutapp.data.pcmunlockpopup.apiservice.PCMUnlockApiService
import com.doubtnutapp.data.pcmunlockpopup.mapper.PCMUnlockDataEntityMapper
import com.doubtnutapp.domain.pcmunlockpopup.entity.PCMUnlockDataEntity
import com.doubtnutapp.domain.pcmunlockpopup.repository.PCMUnlockRepository
import io.reactivex.Single
import javax.inject.Inject

class PCMUnlockRepositoryImpl @Inject constructor(
    private val pcmUnlockDataEntityMapper: PCMUnlockDataEntityMapper,
    private val pcmUnlockApiService: PCMUnlockApiService
) : PCMUnlockRepository {
    override fun getPCMUnlockData(): Single<PCMUnlockDataEntity> {
        return pcmUnlockApiService.getPCMUnlockData().map {
            pcmUnlockDataEntityMapper.map(it.data)
        }
    }
}
