package com.doubtnutapp

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-09-19.
 */
data class DoubtnutNetworkException(
    @Expose
    @SerializedName("meta")
    val meta: Meta = Meta("")
)

data class Meta(
    @Expose
    @SerializedName("message")
    val message: String = ""
)
