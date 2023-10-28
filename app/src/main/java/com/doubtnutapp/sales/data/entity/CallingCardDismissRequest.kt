package com.doubtnutapp.sales.data.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CallingCardDismissRequest(
    @SerializedName("assortment_id")
    val assortmentId: String?,
    @SerializedName("view")
    val view: String?,
    @SerializedName("source")
    val source: String?
)