package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 24/11/20.
 */
@Keep
data class TopDoubtQuestion(
    @SerializedName("message") val text: String?,
    @SerializedName("offset") val offset: Long?,
    @SerializedName("_id") val id: String?,
)
