package com.doubtnutapp.data.resourcelisting.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class ApiPlayListHeader(
    @SerializedName("id") val headerId: String,
    @SerializedName("name") val headerTitle: String,
    @SerializedName("image_url") val headerImageUrl: String?,
    @SerializedName("is_last") val headerIsLast: Int,
    @SerializedName("description") val headerDescription: String?,
    @SerializedName("announcement") val announcement: ApiAnnouncement?,
    @SerializedName("is_lock") val isLock: Int?,
    @SerializedName("subject") val subject: String?
)
