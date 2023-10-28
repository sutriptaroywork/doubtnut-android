package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.domain.newglobalsearch.entities.TabTypeDataEntity
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Keep
@Parcelize
data class AdvancedFilterViewItem(
        val title: String,
        val data : @RawValue List<TabTypeDataEntity>?,
        val shouldShowSeeAll: Boolean,
        override val viewType: Int,
        override val fakeType: String
) : SearchListViewItem(), Parcelable