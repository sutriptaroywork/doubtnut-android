package com.doubtnutapp.data.newlibrary.repository

import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.newlibrary.mapper.LibraryListingMapper
import com.doubtnutapp.data.newlibrary.service.LibraryHomeService
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity
import com.doubtnutapp.domain.newlibrary.entities.HeaderEntity
import com.doubtnutapp.domain.newlibrary.entities.LibraryListingEntity
import com.doubtnutapp.domain.newlibrary.repository.LibraryListingRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
class LibraryListingRepositoryImpl @Inject constructor(
    private val libraryHomeService: LibraryHomeService,
    private val libraryListingMapper: LibraryListingMapper,
    private val userPreference: UserPreference
) : LibraryListingRepository {

    override fun getLibraryListingData(page: Int, id: String, packageDetailsId: String, source: String): Single<LibraryListingEntity> =
        libraryHomeService
            .getLibraryListingData(page, id, userPreference.getUserClass(), packageDetailsId, source)
            .map {
                libraryListingMapper.map(it.data)
            }

    override fun getLibraryListingHeader(id: String): Single<Pair<String, List<HeaderEntity>>> =
        libraryHomeService
            .getLibraryListingHeader(id)
            .map {
                (it.data.pageTitle ?: "") to
                    it.data.headerList.map { headerEntity ->
                        HeaderEntity(
                            headerEntity.id ?: "",
                            headerEntity.name
                                ?: "",
                            headerEntity.isLast, headerEntity.id, AnnouncementEntity()
                        )
                    }
            }
}
