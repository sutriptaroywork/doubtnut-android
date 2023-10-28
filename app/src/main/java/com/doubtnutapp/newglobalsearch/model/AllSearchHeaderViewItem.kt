package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class AllSearchHeaderViewItem(
        val title: String,
        val tabType: String,
        val shouldShowSeeAll: Boolean,
        val tabPosition : Int = 0,
        override val viewType: Int
) : SearchListViewItem(), Parcelable