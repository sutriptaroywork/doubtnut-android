package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 26/05/20.
 */
@Keep
data class LiveClassStats(
    @SerializedName("display_text") val countText: String
)
