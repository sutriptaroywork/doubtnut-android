package com.doubtnutapp.leaderboard.data.entity

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.course.widgets.VideoInfo
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
data class LeaderboardData(
    @SerializedName("background")
    val background: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,
    @SerializedName("back_icon_color")
    val backIconColor: String?,
    @SerializedName("sticky_widgets")
    val stickyWidgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("widgets")
    val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("button_cta_text")
    val buttonCtaText: String?,
    @SerializedName("deeplink")
    val deeplink: String?,
    @SerializedName("leaderboardHelp")
    val leaderboardHelp: LeaderboardHelp?,
    @SerializedName("bottom_data")
    val bottomData: LeaderboardBottomData?,
    @SerializedName("bottom_tabs")
    val bottomTabsData: List<LeaderboardBottomData>?
)

@Parcelize
@Keep
data class LeaderboardHelp(
    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,
    @SerializedName("title_deeplink")
    val titleDeeplink: String?,
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("button_cta_text")
    val buttonCtaText: String?,
    @SerializedName("deeplink")
    val deeplink: String?,
    @SerializedName("video_button_cta_text")
    val videoButtonCtaText: String?,
    @SerializedName("video_info")
    val videoInfo: VideoInfo?,
    @SerializedName("heading")
    val heading: String?,
    @SerializedName("heading_image")
    val headingImage: String?,
    @SerializedName("items")
    val items: List<LeaderboardHelpItem>?,
) : Parcelable

@Parcelize
data class LeaderboardHelpItem(
    @SerializedName("title")
    val title: String?
) : Parcelable

@Keep
data class LeaderboardBottomData(
    @SerializedName("background")
    val background: String?,
    @SerializedName("peek_height")
    val peekHeight: Int?,
    @SerializedName("tab")
    val tab: String?,
    @SerializedName("widgets")
    val widgets: List<WidgetEntityModel<*, *>>?,
)