package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SearchTabsItem(
        val description: String,
        val key: String,
        val isVip: Boolean,
        var filterList: ArrayList<SearchFilter>?
) : Parcelable