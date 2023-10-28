package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.*

@Keep
@Parcelize
open class SearchListViewItem : Parcelable {
    @IgnoredOnParcel
    open val viewType: Int = 0

    @IgnoredOnParcel
    open val fakeType: String = ""

    open fun applyFilter(appliedFilterMap: HashMap<String, String>): Boolean {
        return true
    }

}