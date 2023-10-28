package com.doubtnutapp.data.resourcelisting.repository

import com.doubtnutapp.data.resourcelisting.mapper.ResourceListingMapper
import com.doubtnutapp.data.resourcelisting.service.ResourceListingService
import com.doubtnutapp.domain.resourcelisting.entities.ResourceListingEntity
import com.doubtnutapp.domain.resourcelisting.repository.ResourceListingRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
class ResourceListingRepositoryImpl @Inject constructor(
    private val resourceListingService: ResourceListingService,
    private val resourceListingMapper: ResourceListingMapper
) : ResourceListingRepository {
    override fun getResourceListing(
        page: Int,
        playlistId: String,
        autoPlayData: Boolean,
        packageDetailsId: String?,
        questionIds: List<String>?
    ): Single<ResourceListingEntity> {
        val autoPlayFlag = if (autoPlayData)
            1 else 0
        return resourceListingService.getResources(page, playlistId, autoPlayFlag, packageDetailsId.orEmpty(), questionIds?.joinToString(",")).map {
            resourceListingMapper.map(it.data)
        }
    }

    override fun getResourceListingForVideoTag(page: Int, tag: String, questionId: String, playlistId: String): Single<ResourceListingEntity> {
        return resourceListingService.getResourcesForVideoTag(page, tag, questionId, playlistId).map {
            resourceListingMapper.map(it.data)
        }
    }
}
