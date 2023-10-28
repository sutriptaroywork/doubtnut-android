package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.MicroService

class VideoDownloadRepository(private val microService: MicroService) {

    fun checkValidity(questionId: String) = microService.checkValidity(questionId)

    fun getLicense(questionId: String) = microService.getLicense(questionId)

    fun getDownloadOptions(questionId: String) = microService.getDownloadOptions(questionId)
}
