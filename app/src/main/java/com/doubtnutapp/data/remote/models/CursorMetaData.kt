package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class CursorMetaData(
    @SerializedName("matched") val matched: String?,
    @SerializedName("unanswered") val unanswered: String?,
    @SerializedName("answered") val answered: String?,
    @SerializedName("ugc") val ugc: String?
)
