package com.doubtnutapp.matchquestion.service

import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Url

/**
 * Created by Anand Gaurav on 2019-10-20.
 */
interface UploadImageService {
    @PUT
    fun uploadImage(@Url url: String, @Body requestBody: RequestBody): Single<Unit>
}