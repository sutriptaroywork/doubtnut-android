package com.doubtnutapp.data.common.model.promotional

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-10-10.
 */
@Keep
data class ApiPromotional(
    @SerializedName("scroll_size")
    val scrollSize: String?,
    @SerializedName("list_key")
    val listKey: String?,
    @SerializedName("resource_type")
    val resourceType: String?,
    @SerializedName("data")
    val dataList: List<ApiPromotionalData>?
)
