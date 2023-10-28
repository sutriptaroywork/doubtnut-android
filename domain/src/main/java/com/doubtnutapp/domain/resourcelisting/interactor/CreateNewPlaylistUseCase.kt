package com.doubtnutapp.domain.resourcelisting.interactor

import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import com.doubtnutapp.domain.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-11-25.
 */
class CreateNewPlaylistUseCase @Inject constructor(private val addToPlaylistRepository: AddToPlaylistRepository) : SingleUseCase<Any, Pair<String, String>> {
    override fun execute(param: Pair<String, String>): Single<Any> = addToPlaylistRepository.createPlaylist(param.first, param.second)
}
