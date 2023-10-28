package com.doubtnutapp.data.remote.api.services

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface PdfService {

    @GET
    fun downloadPdf(@Url pdfUrl: String): Single<ResponseBody>
}
