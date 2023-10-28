package com.doubtnutapp.domain.resourcelisting.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.resourcelisting.entities.ResourceListingEntity
import com.doubtnutapp.domain.resourcelisting.repository.ResourceListingRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
class GetResourceListingForVideoTagUseCase @Inject constructor(private val resourceListingRepository: ResourceListingRepository) : SingleUseCase<ResourceListingEntity, GetResourceListingForVideoTagUseCase.Param> {

    override fun execute(param: Param): Single<ResourceListingEntity> = resourceListingRepository.getResourceListingForVideoTag(param.page, param.tag, param.questionId, param.playListId)

    @Keep
    data class Param(val page: Int, val tag: String, val questionId: String, val playListId: String)
}
