package com.doubtnutapp.data.newlibrary.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiLibraryData(
    @SerializedName("view_type") val viewType: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("list") val dataList: List<ApiLibraryListData>?
)
