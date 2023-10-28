package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.ConfigService
import com.doubtnutapp.data.remote.api.services.MicroService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.domain.camerascreen.entity.AudioTooltipResponse
import com.doubtnutapp.domain.homefeed.interactor.ConfigData
import io.reactivex.Single

class AppConfigRepository(
    private val microService: MicroService,
    private val configService: ConfigService
) {

    fun getFeatureConfig(requestedFeatures: HashMap<String, Any>): Single<HashMap<String, Any>> {
        return microService.getFeatureConfig(requestedFeatures)
    }

    fun getConfigData(
        sessionCount: Int,
        postPurchaseSessionCount: Int
    ): Single<ApiResponse<ConfigData>> {
        return configService.getConfigData(sessionCount, postPurchaseSessionCount)
    }

    fun getAudioToolTipData(
    ): Single<ApiResponse<AudioTooltipResponse>> {
        return configService.getAudioTooltipList()
    }
}
