package com.doubtnutapp.data.videoPage.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 28/09/20.
 */

@Keep
data class ApiTabData(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String
)
