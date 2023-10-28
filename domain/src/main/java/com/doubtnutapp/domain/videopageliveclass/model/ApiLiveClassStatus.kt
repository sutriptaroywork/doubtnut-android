package com.doubtnutapp.domain.videopageliveclass.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 23/10/20.
 */

@Keep
data class ApiLiveClassStatus(
    @SerializedName("display") val display: String,
    @SerializedName("key") val key: String,
    @SerializedName("isSelected") val isSelected: Int
)
