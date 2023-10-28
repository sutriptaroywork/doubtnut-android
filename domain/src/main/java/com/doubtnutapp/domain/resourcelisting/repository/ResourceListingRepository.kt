package com.doubtnutapp.domain.resourcelisting.repository

import com.doubtnutapp.domain.resourcelisting.entities.ResourceListingEntity
import io.reactivex.Single

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
interface ResourceListingRepository {
    fun getResourceListing(page: Int, playlistId: String, autoPlayData: Boolean, packageDetailsId: String?, questionIds: List<String>?): Single<ResourceListingEntity>
    fun getResourceListingForVideoTag(page: Int, tag: String, questionId: String, playlistId: String): Single<ResourceListingEntity>
}
