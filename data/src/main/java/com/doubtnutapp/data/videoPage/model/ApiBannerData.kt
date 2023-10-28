package com.doubtnutapp.data.videoPage.model

import androidx.annotation.Keep
import com.doubtnutapp.data.common.model.ApiBannerActionData
import com.google.gson.annotations.SerializedName

@Keep
data class ApiBannerData(
    @SerializedName("type") val type: String,
    @SerializedName("image_url") val link: String,
    @SerializedName("resource_type") val resourceType: String,
    @SerializedName("action_activity") val actionActivity: String,
    @SerializedName("action_data") val actionData: ApiBannerActionData?,
    @SerializedName("size") val size: String?,
    @SerializedName("class") val className: String?,
    @SerializedName("banner_order") val bannerOrder: Int?,
    @SerializedName("position") val position: Int?,
    @SerializedName("page_type") val pageType: String?,
    @SerializedName("is_last") val isLast: Int?
)
