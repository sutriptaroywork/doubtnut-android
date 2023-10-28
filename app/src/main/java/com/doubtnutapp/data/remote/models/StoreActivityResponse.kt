package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 13/1/21.
 */

@Keep
data class StoreActivityResponse(
    @SerializedName("message") val message: String
)
