package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class LibraryBannerEntity(
    val scrollSize: String,
    val listKey: String,
    val resourceType: String,
    val dataList: List<BannerListEntity>
) : DoubtnutViewItem {
    companion object {
        const val type: String = "banner"
    }
}

@Keep
data class BannerListEntity(
    val imageUrl: String,
    val actionActivity: String,
    val bannerPosition: Int,
    val bannerOrder: Int,
    val pageType: String,
    val studentClass: String,
    val isLast: String?,
    val actionData: BannerActionDataEntity
) : DoubtnutViewItem

@Keep
data class BannerActionDataEntity(
    val playlistId: String,
    val playlistTitle: String,
    val isLast: Int,
    val facultyId: Int?,
    val ecmId: Int?,
    val subject: String?
)
