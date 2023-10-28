package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class YoutubeSearchViewItem(
        val title: String,
        val videoId: String,
        val imageUrl: String,
        val width: Int,
        val height: Int,
        @Transient override val viewType: Int,
        @Transient override val fakeType: String
) : SearchListViewItem(), Parcelable