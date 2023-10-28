package com.doubtnutapp.data.common.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-10-06.
 */
@Keep
data class ApiAnnouncement(
    @SerializedName("type") val type: String?,
    @SerializedName("state") val state: Boolean?
)
