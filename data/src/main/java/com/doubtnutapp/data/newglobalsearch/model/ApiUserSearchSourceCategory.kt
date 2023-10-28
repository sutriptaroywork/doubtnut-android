package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.newglobalsearch.entities.ChapterDetails
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.google.gson.annotations.SerializedName

@Keep
data class ApiUserSearchSourceCategory(
    @SerializedName("title") val title: String,
    @SerializedName("tab_type") val tabType: String,
    @SerializedName("size") val size: Int,
    @SerializedName("seeAll") val seeAll: Boolean,
    @SerializedName("chapter_details") val allChapterDetails: ChapterDetails?,
    @SerializedName("list") val searchList: List<ApiUserSearchSource>,
    @SerializedName("list_with_only_filter") val listWithOnlyFilter: List<ApiUserSearchSource>?,
    @SerializedName("title_with_filter") val titleWithFilter: String?,
    @SerializedName("description_with_only_filter") val descriptionWithOnlyFilter: String?,
    @SerializedName("title_with_only_filter") val titleWithOnlyFilter: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("secondary_text") val secondaryText: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("secondary_description") val secondaryDescription: String?,
    @SerializedName("secondary_list") val secondaryList: List<ApiUserSearchSource>?,
    @SerializedName("filter_list") val filterList: List<SearchFilter>?,
    @SerializedName("secondary_filter_list") val secondaryFilterList: List<SearchFilter>?

)
