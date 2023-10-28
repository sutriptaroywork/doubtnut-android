package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Playlist
import com.doubtnutapp.data.remote.models.RevampPlayList
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PlaylistService {

    @POST("/v2/playlist/add-question")
    fun addToPlaylist(
        @Body requestBody: RequestBody
    ): RetrofitLiveData<Any>

    @GET("/v2/playlist/get-list")
    fun getPlaylists(): RetrofitLiveData<ApiResponse<ArrayList<Playlist>>>

    @POST("/v2/playlist/create")
    fun createPlaylistAndAddItem(
        @Body requestBody: RequestBody
    ): RetrofitLiveData<Any>

    @GET("v4/library/getresource")
    fun viewAllPlaylist(
        @Query(value = "page_no") pageNumber: String,
        @Query(value = "id") playlistId: String
    ): RetrofitLiveData<ApiResponse<RevampPlayList>>

    @GET("v4/library/getresource")
    fun viewAllPlaylistNCERT(
        @Query(value = "page_no") pageNumber: Int,
        @Query(value = "id") playlistId: String,
        @Query(value = "chapter_id") chapterId: String,
        @Query(value = "exercise_name") exerciseName: String
    ): RetrofitLiveData<ApiResponse<RevampPlayList>>
}
