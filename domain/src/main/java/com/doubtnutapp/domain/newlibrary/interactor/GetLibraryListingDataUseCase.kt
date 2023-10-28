package com.doubtnutapp.domain.newlibrary.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.newlibrary.entities.LibraryListingEntity
import com.doubtnutapp.domain.newlibrary.interactor.GetLibraryListingDataUseCase.Param
import com.doubtnutapp.domain.newlibrary.repository.LibraryListingRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
class GetLibraryListingDataUseCase @Inject constructor(private val libraryListingRepository: LibraryListingRepository) :
    SingleUseCase<LibraryListingEntity, Param> {

    override fun execute(param: Param): Single<LibraryListingEntity> = libraryListingRepository.getLibraryListingData(param.page, param.playlistId, param.packageDetailsId.orEmpty(), param.source.orEmpty())

    @Keep
    class Param(val page: Int, val playlistId: String, val packageDetailsId: String?, val source: String?)
}
