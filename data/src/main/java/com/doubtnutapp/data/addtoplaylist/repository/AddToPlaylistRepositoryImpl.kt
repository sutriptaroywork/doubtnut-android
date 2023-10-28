package com.doubtnutapp.data.addtoplaylist.repository

import com.doubtnutapp.data.addtoplaylist.service.AddToPlaylistService
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.addtoplaylist.entities.PlaylistEntity
import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-11-22.
 */
class AddToPlaylistRepositoryImpl @Inject constructor(
    private val addToPlaylistService: AddToPlaylistService,
    private val userPreference: UserPreference
) : AddToPlaylistRepository {
    override fun getUserPlaylist(): Single<ArrayList<PlaylistEntity>> {
        return addToPlaylistService.getUserSavedPlaylist().map {
            it.data
        }
    }

    override fun createPlaylist(id: String, playListName: String): Single<Any> {
        val params: HashMap<String, Any> = hashMapOf()
        params["question_id"] = id
        params["playlist_name"] = playListName
        return addToPlaylistService.createPlayList(params.toRequestBody()).map {
            it.data
        }
    }

    override fun submitPlayLists(id: String, playLists: List<String>): Single<Any> {
        val params: HashMap<String, Any> = hashMapOf()
        params["question_id"] = id
        params["playlist_id"] = playLists
        params["student_id"] = userPreference.getUserStudentId()
        return addToPlaylistService.submitPlayLists(params.toRequestBody()).map {
            it.data
        }
    }

    override fun removeFromPlaylist(questionId: String, playlistId: String): Completable {
        val params: HashMap<String, Any> = hashMapOf()
        params["question_id"] = questionId
        params["playlist_id"] = playlistId
        return addToPlaylistService.removeFromPlaylist(params.toRequestBody())
    }
}
