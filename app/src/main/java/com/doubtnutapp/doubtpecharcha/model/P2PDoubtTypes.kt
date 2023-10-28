package com.doubtnutapp.doubtpecharcha.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.common.model.FilterListData
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.ArrayList

@Keep
data class P2PDoubtTypes(
    @SerializedName("primary_tabs") val primaryTabs: List<PrimaryTabData>?,
    @SerializedName("active_primary_tab_id") val activeTab: Int,
    @SerializedName("secondary_tabs") val secondaryTabs: List<TabData>?,
    @SerializedName("active_secondary_tab_id") val activeSecondaryTabId: Int,
    @SerializedName("doubt_data") val doubtData: List<WidgetEntityModel<*, *>>?,
    @SerializedName("no_doubts") val noDoubtsData: NoDoubtsData?,
    @SerializedName("filters") val filters: List<FilterData>?,
    @SerializedName("offset_cursor") val offsetCursor: Int?,
    @SerializedName("rewards") val rewards: Rewards?,
    @SerializedName("page") val page: String?

)

@Keep
data class P2PDoubtData(
    @SerializedName("doubt_data") val doubtData: List<WidgetEntityModel<*, *>>?,
    @SerializedName("offset_cursor") val offsetCursor: Int
)

@Keep
data class PrimaryTabData(
    @SerializedName("primary_tab_id") val tabId: Int?,
    @SerializedName("title") val tabName: String?
)

@Keep
data class TabData(
    @SerializedName("tab_id") val tabId: Int?,
    @SerializedName("title") val tabName: String?
)

@Keep
data class FilterData(
    @SerializedName("title_filter") val titleFilter: String?,
    @SerializedName("selected_option_id") val selectedOptionId: String?,
    @SerializedName("options") val filterItemData: ArrayList<FilterListData.FilterListItem>?
)

@Keep
data class P2pRoomData(
    @SerializedName("ques_data") val quesData: QuestionData?
)

@Keep
data class QuestionData(
    @SerializedName("question_image") val questionImage: String?,
    @SerializedName("question_text") val questionText: String?,
    @SerializedName("title1") val title1: String?,
    @SerializedName("title2") val title2: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("thumbnail_images") val thumbnailImages: List<String>?,
)

@Keep
data class Rewards(
    @SerializedName("level_name") val levelName: String?,
    @SerializedName("badge_name") val badgeName: String?,
    @SerializedName("points_count_text") val pointsCount: String?,
    @SerializedName("points_remaining_text") val pointsRemainingCount: String?,
    @SerializedName("badge_img_url") val badgeImgUrl: String?,
    @SerializedName("profile_img_url") val profileImgUrl: String?,
    @SerializedName("border_img_url") val borderImgUrl: String?,
    @SerializedName("star_img_url") val starImageUrl: String?,
    @SerializedName("bg_start_color") val bgStartColor: String?,
    @SerializedName("bg_end_color") val bgEndColor: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("cta") val cta: Cta?
)

@Keep
data class NoDoubtsData(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("cta") val cta: CtaNoData?
)

@Keep
data class CtaNoData(
    @SerializedName("title") val title: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("title_color") val titleColor: String?
)

@Keep
data class Cta(
    @SerializedName("title") val title: String?,
    @SerializedName("deeplink") val deeplink: String?
)
