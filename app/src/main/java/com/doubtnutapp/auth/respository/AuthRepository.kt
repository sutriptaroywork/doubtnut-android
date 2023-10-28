package com.doubtnutapp.auth.respository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun verifyGoogleAuth(
        token: String
    ): Flow<ApiResponse<Any>> =
        flow { emit(networkService.verifyGoogleAuth(token = token)) }
}
