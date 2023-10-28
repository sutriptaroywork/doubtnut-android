package com.doubtnutapp.similarVideo.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SimilarTopicSearchViewItem(
        val resourceType: String,
        val description: String?,
        val imageUrl: String?,
        val buttonText: String?,
        val buttonBgColor: String?,
        val searchText: String?,
        override val viewType: Int
): Parcelable, RecyclerViewItem