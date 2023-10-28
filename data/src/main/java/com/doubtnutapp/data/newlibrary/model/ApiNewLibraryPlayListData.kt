package com.doubtnutapp.data.newlibrary.model

import androidx.annotation.Keep
import com.doubtnutapp.data.pCBanner.ApiPCBanner
import com.google.gson.annotations.SerializedName

@Keep
data class ApiNewLibraryPlayListData(
    @SerializedName("playlist") val playListData: List<ApiNewLibraryPlayList>,
    @SerializedName("promotional_data") val promotionalData: List<ApiPCBanner>
)
