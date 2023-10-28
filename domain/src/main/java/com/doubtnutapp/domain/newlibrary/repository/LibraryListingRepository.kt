package com.doubtnutapp.domain.newlibrary.repository

import com.doubtnutapp.domain.newlibrary.entities.HeaderEntity
import com.doubtnutapp.domain.newlibrary.entities.LibraryListingEntity
import io.reactivex.Single

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
interface LibraryListingRepository {
    fun getLibraryListingData(page: Int, id: String, packageDetailsId: String, source: String): Single<LibraryListingEntity>
    fun getLibraryListingHeader(id: String): Single<Pair<String, List<HeaderEntity>>>
}
