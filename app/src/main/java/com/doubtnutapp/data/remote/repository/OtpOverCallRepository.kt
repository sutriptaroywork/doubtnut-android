package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.MicroService
import com.doubtnutapp.login.model.OtpOverCall
import io.reactivex.Single
import okhttp3.RequestBody
import javax.inject.Inject

class OtpOverCallRepository @Inject constructor(private val microService: MicroService) {

    fun otpOverCall(requestBody: RequestBody): Single<OtpOverCall> = microService.otpOverCall(requestBody)
}
