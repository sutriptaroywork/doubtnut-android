package com.doubtnutapp.resourcelisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class ResourceListingData(
    val playlist: List<RecyclerViewItem>?,
    val metaInfo: List<PlayListMetaInfoDataModel>?,
    val playListId: String?,
    val selectedContentIndex: Int = 0
)