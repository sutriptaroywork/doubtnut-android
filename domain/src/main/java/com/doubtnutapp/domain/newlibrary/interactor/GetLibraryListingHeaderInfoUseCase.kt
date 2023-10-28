package com.doubtnutapp.domain.newlibrary.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.newlibrary.entities.HeaderEntity
import com.doubtnutapp.domain.newlibrary.repository.LibraryListingRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-30.
 */
class GetLibraryListingHeaderInfoUseCase @Inject constructor(private val libraryListingRepository: LibraryListingRepository) :
    SingleUseCase<Pair<String, List<HeaderEntity>>, String> {

    override fun execute(param: String): Single<Pair<String, List<HeaderEntity>>> = libraryListingRepository.getLibraryListingHeader(param)
}
