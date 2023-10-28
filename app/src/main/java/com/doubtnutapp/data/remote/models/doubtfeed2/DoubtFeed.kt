package com.doubtnutapp.data.remote.models.doubtfeed2

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.Leaderboard
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by devansh on 7/5/21.
 */

@Keep
data class DoubtFeed(
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("widget_info_data") val infoData: DoubtFeedInfo?,
    @SerializedName("widget_benefits_data") val benefitsData: DoubtFeedInfo?,
    @SerializedName("streak_container") val streakContainer: StreakContainer?,
    @SerializedName("is_rank_available") val isRankAvailable: Boolean,
    @SerializedName("rank_data") val rankData: RankData?,
    @SerializedName("topics") val topics: List<Topic>?,
    @SerializedName("carousels") val carousels: List<WidgetEntityModel<*, *>>?,
    @SerializedName("bottom_sheet_data") val bottomSheetData: List<WidgetEntityModel<*, *>>,
    @SerializedName("back_press_popup_data") val backPressPopupData: DfPopupData?,
    @SerializedName("top_pane") val topPaneData: TopPaneData?,
    @SerializedName("leaderboard_deeplink") val leaderboardDeeplink: String,
    @SerializedName("leaderboard_image") val leaderboardImage: String,
    @SerializedName("is_leaderboard_available") val isLeaderboardAvailable: Boolean,
    @SerializedName("leaderboard_container") val leaderboardContainer: Leaderboard?,
    @SerializedName("view_leaderboard_text") val viewLeaderboardText: String,
    @SerializedName("previous_doubts_text") val previousDoubtsText: String?,
    @SerializedName("previous_doubts_deeplink") val previousDoubtsDeeplink: String?,
) {
    companion object {
        const val TYPE_FIRST_TIME = "first_time"
        const val TYPE_NO_CURRENT_GOAL = "no_current_goal"
        const val TYPE_ON_GOING = "on_going"
    }
}

@Keep
data class StreakContainer(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("know_more") val knowMore: String,
    @SerializedName("deeplink") val deeplink: String,
)

@Keep
data class RankData(
    @SerializedName("rank_text") val rankText: String?,
    @SerializedName("rank") val rank: String?,
)

@Keep
@Parcelize
data class TopPaneData(
    @SerializedName("type") val type: String,
    @SerializedName("heading_image") val headingImage: String,
    @SerializedName("heading_text") val headingText: String,
    @SerializedName("description") val description: String,
    @SerializedName("button_text") val buttonText: String,
    @SerializedName("button_bg_color") val buttonBgColor: String,
    @SerializedName("button_deeplink") val buttonDeeplink: String,
) : Parcelable
