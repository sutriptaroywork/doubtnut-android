package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class YoutubeBannerViewItem(
        val title: String,
        val tabType: String,
        val shouldShowSeeAll: Boolean,
        override val viewType: Int,
        override val fakeType: String
) : SearchListViewItem(), Parcelable