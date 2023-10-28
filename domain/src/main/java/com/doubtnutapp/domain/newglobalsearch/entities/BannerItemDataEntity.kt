package com.doubtnutapp.domain.newglobalsearch.entities

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class BannerItemDataEntity(

    val ccmId: Long?,
    val assortmentId: Long?,
    val demoVideoThumbnail: String?,
    val deeplinkUrl: String?,
    val type: String?
) : Parcelable

@Keep
@Parcelize
data class TabTypeDataEntity(
    val key: String?,
    val value: String?
) : Parcelable
