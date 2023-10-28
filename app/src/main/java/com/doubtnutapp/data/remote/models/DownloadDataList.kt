package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class DownloadDataList(
    @SerializedName("package") val packageNameData: String?,
    @SerializedName("level1") val levelOneData: String?,
    @SerializedName("level2") val levelTwoData: String?,
    @SerializedName("location") var downloadPath: String?
)
