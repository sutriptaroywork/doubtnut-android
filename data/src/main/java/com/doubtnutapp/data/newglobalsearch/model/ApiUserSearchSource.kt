package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiUserSearchSource(
    @SerializedName("_source") val userSearchPlaylist: ApiUserSearchPlaylist
)
