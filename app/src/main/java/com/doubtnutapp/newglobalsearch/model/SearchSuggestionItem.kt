package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SearchSuggestionItem(
        val displayText: String,
        val variantId : Long,
        val id: String,
        val version: String,
        @Transient override val viewType: Int,
        @Transient override  val fakeType: String
) : SearchListViewItem(), Parcelable