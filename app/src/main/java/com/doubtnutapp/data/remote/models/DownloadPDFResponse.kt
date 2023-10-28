package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class DownloadPDFResponse(
    @SerializedName("filterType") val filterType: String,
    @SerializedName("data-list") val dataList: ArrayList<DownloadDataList>,
    @SerializedName("cdn_url") val cdnPath: String
)
