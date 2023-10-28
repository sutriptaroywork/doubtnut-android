package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NewApiUserSearchData(
    @SerializedName("tabs") val tabsList: List<ApiSearchTab>,
    @SerializedName("list") val searchList: List<ApiUserSearchSourceCategory>,
    @SerializedName("ias_facets") val facets: List<ApiSearchFacet>?,
    @SerializedName("isVipUser") val isVipUser: Boolean,
    @SerializedName("landing_facet_type") val landingFacet: String?,
    @SerializedName("banner_data") val bannerData: BannerData?,
    @SerializedName("feed_data") val feedData: FeedbackData?
)
