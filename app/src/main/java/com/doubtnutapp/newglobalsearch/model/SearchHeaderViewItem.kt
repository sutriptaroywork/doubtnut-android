package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SearchHeaderViewItem (
        val title: String,
        val imageUrl: String,
        override val viewType: Int
) : SearchListViewItem(), Parcelable