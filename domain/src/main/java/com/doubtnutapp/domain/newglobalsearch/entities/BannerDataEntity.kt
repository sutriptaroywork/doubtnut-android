package com.doubtnutapp.domain.newglobalsearch.entities

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.android.parcel.RawValue

@Keep
@Parcelize
data class BannerDataEntity(

    val text: String?,
    val type: String?,
    val position: Int?,
    val list: @RawValue List<BannerItemDataEntity>? = null
) : Parcelable

@Keep
@Parcelize
data class FeedBackDataEntity(
    val showTime: Int?,
    val title: String?,
    val data: @RawValue List<TabTypeDataEntity>? = null
) : Parcelable
