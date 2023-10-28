package com.doubtnutapp.domain.resourcelisting.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class ResourceListingEntity(
    val playlist: List<RecyclerDomainItem>?,
    val metaInfo: List<PlayListMetaInfoEntity>?,
    val playListId: String?
)
