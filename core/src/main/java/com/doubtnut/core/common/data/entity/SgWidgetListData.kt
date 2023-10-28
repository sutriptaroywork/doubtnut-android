package com.doubtnut.core.common.data.entity

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class SgWidgetListData(
    @SerializedName("title") val title: String?,
    @SerializedName("new_group_container") val newGroupContainer: NewGroupContainer?,
    @SerializedName("is_widget_available") val isWidgetAvailable: Boolean,
    @SerializedName("is_my_groups_available") val isMyGroupsAvailable: Boolean,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("no_widget_container") val noWidgetContainer: NoWidgetContainer?,
    @SerializedName("is_search_enabled") val isSearchEnabled: Boolean,
    @SerializedName("min_search_characters") val minSearchCharacters: Int?,
    @SerializedName("is_reached_end") val isReachedEnd: Boolean,
    @SerializedName("page") val page: Int,
    @SerializedName("cta") val cta: BottomCta?,
    @SerializedName("search_text") val searchText: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("join_heading") val joinHeading: String?,
)

@Keep
data class NoWidgetContainer(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("image") val image: String?,
)

@Keep
data class NewGroupContainer(
    @SerializedName("title") val title: String,
    @SerializedName("image") val image: String,
    @SerializedName("deeplink") val deeplink: String,
)

@Keep
data class BottomCta(
    @SerializedName("title") val title: String,
    @SerializedName("deeplink") val deeplink: String,
)