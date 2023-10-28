package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.course.widgets.VpaWidget
import com.doubtnutapp.data.remote.api.services.MicroService
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.WalletData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 24/11/20.
 */
class WalletRepository @Inject constructor(
    private val microService: MicroService,
    private val networkService: NetworkService
) {

    fun fetchWalletData(): Flow<ApiResponse<WalletData>> =
        flow { emit(microService.fetchWalletData()) }

    fun fetchVpaInfo(): Flow<ApiResponse<VpaWidget.Account>> =
        flow { emit(networkService.fetchVpaInfo()) }
}
