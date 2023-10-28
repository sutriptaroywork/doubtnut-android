package com.doubtnutapp.data.profile.watchedvideo.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.profile.watchedvideo.model.ApiWatchedVideoDataList
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface WatchedVideoService {

    @GET("/v7/library/getcustomplaylist")
    fun getWatchedVideosList(
        @Query("page_no") pageNumber: Int,
        @Query("playlist_name") playlistName: String
    ): Single<ApiResponse<ApiWatchedVideoDataList>>

    @POST("v3/feedback/video-add")
    fun watchedVideosLiked(@Body requestBody: RequestBody): Completable

    @POST("v1/sharing/whatsapp")
    fun watchedVideosShared(@Body requestBody: RequestBody): Completable
}
