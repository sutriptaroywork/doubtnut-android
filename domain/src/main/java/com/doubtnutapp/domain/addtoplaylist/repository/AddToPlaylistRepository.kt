package com.doubtnutapp.domain.addtoplaylist.repository

import com.doubtnutapp.domain.addtoplaylist.entities.PlaylistEntity
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Anand Gaurav on 2019-11-22.
 */
interface AddToPlaylistRepository {
    fun getUserPlaylist(): Single<ArrayList<PlaylistEntity>>
    fun createPlaylist(id: String, playListName: String): Single<Any>
    fun submitPlayLists(id: String, playLists: List<String>): Single<Any>
    fun removeFromPlaylist(questionId: String, playlistId: String): Completable
}
