package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import okhttp3.RequestBody
import javax.inject.Inject

/**
 * Created by
anandwana001 on
06-09-2018 at
11:43 AM.
 */
class InviteRepository @Inject constructor(private val networkService: NetworkService) {

    fun sendAppInvitationData(params: RequestBody) = networkService.sendInvitationData(params)
}
