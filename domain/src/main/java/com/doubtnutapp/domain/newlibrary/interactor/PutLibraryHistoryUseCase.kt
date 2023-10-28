package com.doubtnutapp.domain.newlibrary.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.newlibrary.entities.LibraryHistoryEntity
import com.doubtnutapp.domain.newlibrary.repository.LibraryHistoryRepository
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-10-17.
 */
class PutLibraryHistoryUseCase @Inject constructor(private val libraryHistoryRepository: LibraryHistoryRepository) : CompletableUseCase<LibraryHistoryEntity> {
    override fun execute(param: LibraryHistoryEntity): Completable = libraryHistoryRepository.putLibraryHistory(param)
}
