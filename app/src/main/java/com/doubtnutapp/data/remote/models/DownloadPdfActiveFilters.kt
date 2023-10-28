package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class DownloadPdfActiveFilters(
    @SerializedName("package")val packageName: String?,
    @SerializedName("level1") val levelOne: String?,
    @SerializedName("level2") val levelTwo: String?
)
