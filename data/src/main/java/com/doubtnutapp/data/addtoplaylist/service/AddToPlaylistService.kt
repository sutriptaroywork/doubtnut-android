package com.doubtnutapp.data.addtoplaylist.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.domain.addtoplaylist.entities.PlaylistEntity
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Created by Anand Gaurav on 2019-11-22.
 */
interface AddToPlaylistService {
    @GET("/v2/playlist/get-list")
    fun getUserSavedPlaylist(): Single<ApiResponse<ArrayList<PlaylistEntity>>>

    @POST("/v2/playlist/create")
    fun createPlayList(
        @Body requestBody: RequestBody
    ): Single<ApiResponse<Any>>

    @POST("v2/playlist/playlistWrapper")
    fun submitPlayLists(
        @Body requestBody: RequestBody
    ): Single<ApiResponse<Any>>

    @POST("v2/playlist/remove-question")
    fun removeFromPlaylist(
        @Body requestBody: RequestBody
    ): Completable
}
