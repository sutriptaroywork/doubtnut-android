package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiSearchFacet(
    @SerializedName("facet_type") val facetType: String? = null,
    @SerializedName("is_multi_select") val isMultiSelect: Boolean? = null,
    @SerializedName("display") val display: String? = null,
    @SerializedName("local") val local: Boolean? = null,
    @SerializedName("is_selected") val isSelected: Boolean? = null,
    @SerializedName("data") val data: List<SearchFacetTopic>? = null
)

@Keep
data class SearchFacetTopic(
    @SerializedName("display") val display: String? = null,
    @SerializedName("is_selected") val isSelected: Boolean? = null,
    @SerializedName("data") val data: List<String>? = null
)

@Keep
data class BannerData(
    @SerializedName("title") val text: String?,
    @SerializedName("tab_type") val type: String?,
    @SerializedName("position") val position: Int?,
    @SerializedName("list") val list: List<BannerItemData>? = null
)

@Keep
data class BannerItemData(
    @SerializedName("ccm_id") val ccmId: Long?,
    @SerializedName("assortment_id") val assortmentId: Long?,
    @SerializedName("demo_video_thumbnail") val demoVideoThumbnail: String?,
    @SerializedName("deeplink_url") val deeplinkUrl: String?,
    @SerializedName("type") val type: String?
)

@Keep
data class FeedbackData(
    @SerializedName("show_time") val showTime: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("data") val data: List<TabTypeData>?
)

@Keep
data class TabTypeData(
    @SerializedName("key") val key: String?,
    @SerializedName("value") val value: String?
)
