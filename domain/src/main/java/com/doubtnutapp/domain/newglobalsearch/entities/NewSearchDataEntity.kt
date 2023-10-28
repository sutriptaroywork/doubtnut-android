package com.doubtnutapp.domain.newglobalsearch.entities

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.android.parcel.RawValue

@Keep
@Parcelize
data class NewSearchDataEntity(
    val tabList: @RawValue List<SearchTabsEntity>,
    val searchResultsCategoriesList: @RawValue List<NewSearchCategorizedDataEntity>,
    var youtubeSearchResultsData: @RawValue YTSearchDataEntity? = null,
    val isVipUser: Boolean,
    val landingFacet: String?,
    val bannerData: BannerDataEntity?,
    val feedData: FeedBackDataEntity?
) : Parcelable
