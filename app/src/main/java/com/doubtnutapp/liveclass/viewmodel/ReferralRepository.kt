package com.doubtnutapp.liveclass.viewmodel

import com.doubtnutapp.data.remote.models.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody

class ReferralRepository(private val referralService: ReferralService) {

    fun getReferralData(
        type: String?,
        assortmentType: String?,
        assortmentId: String?
    ): Flow<ApiResponse<ReferralData>> =
        flow { emit(referralService.getReferralData(type, assortmentType, assortmentId)) }

    fun postFeed(reqBody: RequestBody): Flow<ApiResponse<ShareFeedData>> =
        flow { emit(referralService.postFeed(reqBody)) }
}