package com.doubtnutapp.widgetmanager.widgets.tablist

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

class TabListWidgetModel : WidgetEntityModel<TabListWidgetData, WidgetAction>()

@Keep
data class TabListWidgetData(
        @SerializedName("_id") val id: String,
        @SerializedName("title") val title: String,
        @SerializedName("items") val items: List<TabListItemsData>,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("sharing_message") val sharingMessage: String?,
        @SerializedName("show_view_all") val showViewAll: Int
) : WidgetData()

@Keep
data class TabListItemsData(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String,
        @SerializedName("playlist") val playlist: Int,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("show_whatsapp") val showWhatsapp: Boolean,
        @SerializedName("show_video") val showVideo: Boolean,
        @SerializedName("image_url") val imageUrl: String,
        @SerializedName("card_width") val cardWidth: String,
        @SerializedName("items") var items: List<TabItemData>,
        @SerializedName("card_ratio") val cardRatio: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("view_type") val viewType: String?,
        @SerializedName("aspect_ratio") val aspectRatio: String
)

data class TabItemData(
        @SerializedName("id") val id: String,
        @SerializedName("title") val title: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("resource_type") val resourceType: String?,
        @SerializedName("deeplink") val deeplink: String?
)