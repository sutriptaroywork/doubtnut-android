package com.doubtnutapp.data.remote.models.userstatus

import com.google.gson.annotations.SerializedName

data class StatusApiResponse(
    @SerializedName("story") val statusData: List<UserStatus>?,
    @SerializedName("offsetCursor") val offsetCursor: String?,
    @SerializedName("pageSize") val pageSize: Int?
)
