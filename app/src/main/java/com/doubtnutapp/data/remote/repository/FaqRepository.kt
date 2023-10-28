package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.*
import io.reactivex.Single
import javax.inject.Inject

class FaqRepository @Inject constructor(private val networkService: NetworkService) {

    fun getFaqData(bucket: String?, priority: String?): Single<ApiResponse<ApiFaqData>> = networkService.getFaqData(bucket, priority)
}
