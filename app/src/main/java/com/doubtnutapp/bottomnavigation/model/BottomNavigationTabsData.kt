package com.doubtnutapp.bottomnavigation.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BottomNavigationTabsData(

    @SerializedName("tab1") val tab1: TabData?,
    @SerializedName("tab2") val tab2: TabData?,
    @SerializedName("tab3") val tab3: TabData?,
    @SerializedName("tab4") val tab4: TabData?,
) {

    @Keep
    data class TabData(
        @SerializedName("name") val name: String?,
        @SerializedName("icon_url_active") val iconUrlActive: String?,
        @SerializedName("icon_url_inactive") val iconUrlInactive: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("show_notification_badge") val showNotificationBadge: Boolean?,
        @SerializedName("tag") val tag: String?,
        @SerializedName("is_selectable") val isSelectable: Boolean?,
        @SerializedName("last_updated_time") val lastUpdatedTime: Long?,
    )
}