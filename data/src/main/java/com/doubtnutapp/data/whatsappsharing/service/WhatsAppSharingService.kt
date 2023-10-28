package com.doubtnutapp.data.whatsappsharing.service

import io.reactivex.Completable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface WhatsAppSharingService {

    @POST("v1/sharing/whatsapp")
    fun videoShared(@Body requestBody: RequestBody): Completable
}
