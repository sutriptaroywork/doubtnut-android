package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class TrendingSearchDataListViewItem(
        val header: String,
        val dataType: String,
        val imageUrl: String,
        val widgetType: String,
        var position: Int = -1,
        override val viewType: Int,
        val playlist: List<TrendingSearchFeedViewItem>
) : Parcelable, TrendingSearchWidgetViewItem