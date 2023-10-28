package com.doubtnutapp.domain.addtoplaylist.interactor

import com.doubtnutapp.domain.addtoplaylist.entities.PlaylistEntity
import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import com.doubtnutapp.domain.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-11-22.
 */
class GetUserPlaylistUseCase @Inject constructor(private val addToPlaylistRepository: AddToPlaylistRepository) : SingleUseCase<ArrayList<PlaylistEntity>, Unit> {
    override fun execute(param: Unit): Single<ArrayList<PlaylistEntity>> = addToPlaylistRepository.getUserPlaylist()
}
