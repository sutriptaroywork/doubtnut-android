package com.doubtnutapp.domain.whatsappsharing.repository

import io.reactivex.Completable
import io.reactivex.Single
import java.io.InputStream

interface WhatsAppShareRepository {

    fun getDeepLink(
        channel: String,
        campaign: String,
        type: String?,
        controlParams: HashMap<String, String>?
    ): Single<String>

    fun getShareableImageUrl(
        imageInputStream: InputStream,
        canvasBgColor: String,
        authority: String
    ): Single<String>

    fun videoShared(questionId: String): Completable
}
