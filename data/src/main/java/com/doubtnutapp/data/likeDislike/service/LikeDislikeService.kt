package com.doubtnutapp.data.likeDislike.service

import io.reactivex.Completable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface LikeDislikeService {

    @POST("v3/feedback/video-add")
    fun LikeDislike(@Body requestBody: RequestBody): Completable
}
