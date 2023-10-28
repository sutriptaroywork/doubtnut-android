package com.doubtnutapp.domain.common.entities

import androidx.annotation.Keep

@Keep
data class SimilarPCBannerEntity(
    val index: Int,
    val listKey: String,
    val dataList: List<PCDataListEntity>,
    val resourceType: String
) : DoubtnutViewItem {
    companion object {
        const val resourceType: String = "pcBanner"
    }
}

@Keep
data class PCDataListEntity(
    val imageUrl: String,
    val actionActivity: String,
    val bannerPosition: Int,
    val bannerOrder: Int,
    val pageType: String,
    val studentClass: String,
    val actionData: BannerPCActionDataEntity
)

@Keep
data class BannerPCActionDataEntity(
    val playlistId: String,
    val playlistTitle: String,
    val isLast: Int?,
    val eventKey: String?,
    val facultId: Int?,
    val ecmId: Int?,
    val subject: String?
)
