package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SeeAllButtonViewItem(val text: String, override val viewType: Int) : SearchListViewItem(), Parcelable