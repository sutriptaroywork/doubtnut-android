package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.PlaylistService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Playlist
import com.doubtnutapp.data.remote.models.RevampPlayList
import okhttp3.RequestBody

class PlaylistRepository(val playlistService: PlaylistService) {

    fun viewAllPlaylist(pageNumber: String, playlistId: String):
        RetrofitLiveData<ApiResponse<RevampPlayList>> =
        playlistService.viewAllPlaylist(pageNumber, playlistId)

    fun addToPlaylist(body: RequestBody):
        RetrofitLiveData<Any> = playlistService.addToPlaylist(body)

    fun getPlaylists(): RetrofitLiveData<ApiResponse<ArrayList<Playlist>>> =
        playlistService.getPlaylists()

    fun createPlaylistAndAddItem(body: RequestBody): RetrofitLiveData<Any> =
        playlistService.createPlaylistAndAddItem(body)

    fun viewAllPlaylistNCERT(
        playlistId: String,
        chapterId: String,
        exerciseName: String,
        pageNumber: Int
    ): RetrofitLiveData<ApiResponse<RevampPlayList>> =
        playlistService.viewAllPlaylistNCERT(pageNumber, playlistId, chapterId, exerciseName)
}
