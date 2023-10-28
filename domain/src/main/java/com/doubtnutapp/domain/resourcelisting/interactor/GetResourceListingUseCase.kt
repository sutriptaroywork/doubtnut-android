package com.doubtnutapp.domain.resourcelisting.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.resourcelisting.entities.ResourceListingEntity
import com.doubtnutapp.domain.resourcelisting.interactor.GetResourceListingUseCase.Param
import com.doubtnutapp.domain.resourcelisting.repository.ResourceListingRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
class GetResourceListingUseCase @Inject constructor(private val resourceListingRepository: ResourceListingRepository) : SingleUseCase<ResourceListingEntity, Param> {

    override fun execute(param: Param): Single<ResourceListingEntity> = resourceListingRepository.getResourceListing(param.page, param.playlistId, param.autoPlayData, param.packageDetailsId, param.questionIds)

    @Keep
    data class Param(
        val page: Int,
        val playlistId: String,
        val packageDetailsId: String?,
        val autoPlayData: Boolean = false,
        val questionIds: List<String>? = null,
    )
}
