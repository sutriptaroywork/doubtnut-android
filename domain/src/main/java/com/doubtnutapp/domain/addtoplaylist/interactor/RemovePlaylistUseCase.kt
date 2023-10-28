package com.doubtnutapp.domain.addtoplaylist.interactor

import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import com.doubtnutapp.domain.base.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-01.
 */
class RemovePlaylistUseCase @Inject constructor(private val addToPlaylistRepository: AddToPlaylistRepository) : CompletableUseCase<Pair<String, String>> {

    override fun execute(param: Pair<String, String>): Completable = addToPlaylistRepository.removeFromPlaylist(param.first, param.second)
}
