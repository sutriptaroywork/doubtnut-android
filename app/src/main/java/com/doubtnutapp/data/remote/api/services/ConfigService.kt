package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.Constants
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.domain.camerascreen.entity.AudioTooltipResponse
import com.doubtnutapp.domain.homefeed.interactor.ConfigData
import com.doubtnutapp.orDefaultValue
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ConfigService {

    @GET("/v1/config/settings")
    fun getConfigData(
        @Query("session_count") sessionCount: Int,
        @Query("post_purchase_session_count") postPurchaseSessionCount: Int,
        @Query("gaid") gaid: String = defaultPrefs().getString(Constants.GAID, "").orDefaultValue()
    ): Single<ApiResponse<ConfigData>>

    @GET("v1/audio-tooltip/files")
    fun getAudioTooltipList(): Single<ApiResponse<AudioTooltipResponse>>
}
