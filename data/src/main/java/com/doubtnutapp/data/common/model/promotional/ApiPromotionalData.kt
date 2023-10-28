package com.doubtnutapp.data.common.model.promotional

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-10-10.
 */
@Keep
data class ApiPromotionalData(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("action_activity") val actionActivity: String?,
    @SerializedName("position") val bannerPosition: Int?,
    @SerializedName("banner_order") val bannerOrder: Int?,
    @SerializedName("page_type") val pageType: String?,
    @SerializedName("class") val studentClass: String?,
    @SerializedName("is_last") val isLast: String?,
    @SerializedName("size") val size: String?,
    @SerializedName("action_data") val actionData: ApiPromotionalActionData?
)
