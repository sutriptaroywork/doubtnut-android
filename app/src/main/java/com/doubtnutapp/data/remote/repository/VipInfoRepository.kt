package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import javax.inject.Inject

class VipInfoRepository @Inject constructor(private val networkService: NetworkService) {

    fun getVipPurchaseInfo() = networkService.getVipPurchaseInfo().map { it.data }
}
