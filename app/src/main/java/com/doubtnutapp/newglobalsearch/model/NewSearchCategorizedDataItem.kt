package com.doubtnutapp.newglobalsearch.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.domain.newglobalsearch.entities.ChapterDetails
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.domain.newglobalsearch.entities.SearchListItem
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
@Keep
data class NewSearchCategorizedDataItem(
        val title: String,
        val tabType: String,
        val size: Int,
        val shouldShowSeeAll: Boolean,
        val chapterDetails: ChapterDetails?,
        val dataList: List<SearchListViewItem>,
        val dataListWithFilter:  @RawValue List<SearchListViewItem>?,
        val titleWithFilter:  @RawValue String?,
        val descriptionWithOnlyFilter:  @RawValue String?,
        val titleWithOnlyFilter:  @RawValue String?,
        val text: String?,
        val secondaryText: String?,
        val description: String?,
        val secondaryDescription: String?,
        val secondaryList: List<SearchListViewItem>?,
        val filterList: List<SearchFilter>?,
        val secondaryFilterList: List<SearchFilter>?,
        override val viewType: Int,
        override val fakeType: String
) : SearchListViewItem(), Parcelable