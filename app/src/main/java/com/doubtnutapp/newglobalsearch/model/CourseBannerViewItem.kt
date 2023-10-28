package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class CourseBannerViewItem(
        val title: String,
        val tabType: String,
        val imageUrl : String?,
        val deepLinkUrl : String?,
        val shouldShowSeeAll: Boolean,
        override val viewType: Int,
        override val fakeType: String
) : SearchListViewItem(), Parcelable