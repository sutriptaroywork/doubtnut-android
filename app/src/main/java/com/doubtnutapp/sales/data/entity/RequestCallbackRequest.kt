package com.doubtnutapp.sales.data.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RequestCallbackRequest(
    @SerializedName("assortment_id")
    val assortmentId: String
)