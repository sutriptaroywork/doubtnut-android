package com.doubtnutapp.data.newlibrary.repository

import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.domain.newlibrary.entities.LibraryHistoryEntity
import com.doubtnutapp.domain.newlibrary.repository.LibraryHistoryRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-10-17.
 */
class LibraryHistoryRepositoryImpl @Inject constructor(
    private val userPreference: UserPreference
) : LibraryHistoryRepository {

    override fun putLibraryHistory(libraryHistoryEntity: LibraryHistoryEntity): Completable =
        Completable.fromCallable { userPreference.putLibraryHistory(libraryHistoryEntity) }

    override fun getLibraryHistory(): Single<LibraryHistoryEntity?> = Single.fromCallable { userPreference.getLibraryHistory() }
}
