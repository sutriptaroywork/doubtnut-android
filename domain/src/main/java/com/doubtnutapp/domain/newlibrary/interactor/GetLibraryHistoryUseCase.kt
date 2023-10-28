package com.doubtnutapp.domain.newlibrary.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.newlibrary.entities.LibraryHistoryEntity
import com.doubtnutapp.domain.newlibrary.repository.LibraryHistoryRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-10-16.
 */
class GetLibraryHistoryUseCase @Inject constructor(private val libraryHistoryRepository: LibraryHistoryRepository) : SingleUseCase<LibraryHistoryEntity?, Unit> {
    override fun execute(param: Unit): Single<LibraryHistoryEntity?> = libraryHistoryRepository.getLibraryHistory()
}
