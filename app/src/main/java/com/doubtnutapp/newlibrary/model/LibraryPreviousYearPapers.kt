package com.doubtnutapp.newlibrary.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 26/11/21
 */

@Keep
data class LibraryPreviousYearPapers(
    @SerializedName("page_title") val pageTitle: String,
    @SerializedName("tab_data") val tabData: List<LibraryTabItem>
)

@Keep
data class LibraryTabItem(
    @SerializedName("tab_text") val tabText: String,
    @SerializedName("tab_id") val tabId: String,
    @SerializedName("is_selected") val isSelected: Boolean,
    @SerializedName("filter_data") val filterData: List<LibraryPreviousYearPapersFilter>
)
