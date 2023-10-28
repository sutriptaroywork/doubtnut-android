package com.doubtnutapp.domain.newlibrary.repository

import com.doubtnutapp.domain.newlibrary.entities.LibraryHistoryEntity
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by Anand Gaurav on 2019-10-16.
 */
interface LibraryHistoryRepository {
    fun putLibraryHistory(libraryHistoryEntity: LibraryHistoryEntity): Completable
    fun getLibraryHistory(): Single<LibraryHistoryEntity?>
}
