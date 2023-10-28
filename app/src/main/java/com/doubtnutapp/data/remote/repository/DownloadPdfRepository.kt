package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.DownloadPDFResponse
import okhttp3.RequestBody
import javax.inject.Inject

class DownloadPdfRepository @Inject constructor(private val networkService: NetworkService) {

    fun getPdfDownloads(params: RequestBody): RetrofitLiveData<ApiResponse<DownloadPDFResponse>> =
        networkService.pdfDownloads(params)
}