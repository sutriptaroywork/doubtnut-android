package com.doubtnutapp.data.base.service

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

interface DownloadService {
    @GET
    fun downloadIcon(@Url iconUrl: String): Single<ResponseBody>

    @GET
    fun downloadSampleCropImage(@Url iconUrl: String): Single<ResponseBody>
}
