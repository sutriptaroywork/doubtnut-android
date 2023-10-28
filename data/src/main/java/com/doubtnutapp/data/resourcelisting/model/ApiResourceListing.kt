package com.doubtnutapp.data.resourcelisting.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class ApiResourceListing(
    @SerializedName("playlist") val playlist: List<ApiQuestionMeta>?,
    @SerializedName("meta_info") val metaInfo: List<ApiPlayListMetaInfo>?,
    @SerializedName("library_playlist_id") val playListId: String?,
    @SerializedName("package_details_id") val packageDetailsId: String?
)
