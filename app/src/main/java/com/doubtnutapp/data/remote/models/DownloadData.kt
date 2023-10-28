package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

// TODO check whether it is possible that dataList will be null
data class DownloadData(
    @SerializedName("filterType") val filterType: String,
    @SerializedName("data-list") val dataList: ArrayList<DownloadDataList>,
    @SerializedName("cdn_url") val cdnPath: String

)
