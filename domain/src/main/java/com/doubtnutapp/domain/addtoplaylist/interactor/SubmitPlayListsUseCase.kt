package com.doubtnutapp.domain.addtoplaylist.interactor

import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import com.doubtnutapp.domain.base.SingleUseCase
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-11-26.
 */
class SubmitPlayListsUseCase @Inject constructor(private val addToPlaylistRepository: AddToPlaylistRepository) : SingleUseCase<Any, Pair<String, List<String>>> {
    override fun execute(param: Pair<String, List<String>>): Single<Any> = addToPlaylistRepository.submitPlayLists(param.first, param.second)
}
