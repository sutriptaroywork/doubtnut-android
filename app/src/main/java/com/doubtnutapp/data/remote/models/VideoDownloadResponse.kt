package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class VideoDownloadResponse(
    @SerializedName("cdnUrl") val cdnUrl: String?,
    @SerializedName("thumbnail") val thumbnail: String,
    @SerializedName("playlist") val playlist: String,
    @SerializedName("title") val title: String,
    @SerializedName("options") val options: List<Option>,
    @SerializedName("aspectRatio") val aspectRatio: String?,
    @SerializedName("mediaType") val mediaType: String? = null
) {
    data class Option(
        @SerializedName("display") val display: String,
        @SerializedName("width") val width: Int,
        @SerializedName("height") val height: Int
    )
}

data class VideoLicenseResponse(
    @SerializedName("validity") val validity: Date?,
    @SerializedName("licenseUrl") val licenseUrl: String?
)
