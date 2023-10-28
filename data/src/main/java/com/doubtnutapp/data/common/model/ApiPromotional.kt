package com.doubtnutapp.data.common.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
@Keep
data class ApiPromotional(
    @SerializedName("index") val index: Int,
    @SerializedName("list_key") val listKey: String,
    @SerializedName("type") val type: String,
    @SerializedName("data") val dataList: List<ApiPromotionalData>,
    @SerializedName("resource_type") val resourceType: String = "banner"
)
