package com.doubtnutapp.widgets.data.entities

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class BaseWidgetData(
    @SerializedName("lottie_url")
    val lottieUrl: String?,

    @SerializedName("scholarship_test_id")
    val scholarshipTestId: String?,
    @SerializedName("test_id")
    val testId: String?,

    @SerializedName("title", alternate = ["title_one"])
    val title: String?,
    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("title_text_size", alternate = ["title_one_text_size"])
    val titleTextSize: String?,
    @SerializedName("title_text_color", alternate = ["title_one_text_color"])
    val titleTextColor: String?,

    @SerializedName("action_text")
    val actionText: String?,
    @SerializedName("action_text_size")
    val actionTextSize: String?,
    @SerializedName("action_text_color")
    val actionTextColor: String?,
    @SerializedName("action_deep_link", alternate = ["action_deeplink"])
    val actionDeepLink: String?,

    @SerializedName("start_time_in_millis")
    val startTimeInMillis: Long?,

    @SerializedName("padding_bottom")
    val paddingBottom: Int?,
    @SerializedName("dialog_width_padding")
    val dialogWidthPadding: Int?,

    @SerializedName("show_close_btn")
    val showCloseBtn: Boolean?,
    @SerializedName("show_top_drag_icon")
    val showTopDragIcon: Boolean?,
    @SerializedName("widgets")
    val widgets: List<WidgetEntityModel<*, *>?>?,

    @SerializedName("tab_data")
    val tabData: TabData?,

    @SerializedName("back_press_deeplink")
    val backPressDeeplink: String?,

    @SerializedName("extra_params")
    val extraParams: HashMap<String, Any>? = null,

    @SerializedName("cta")
    val cta: Cta?

)

@Keep
data class TabData(
    @SerializedName("items")
    val items: List<TabItem>?,
    @SerializedName("mode")
    val mode: Int?,
    @SerializedName("style")
    val style: Int?,
)

@Keep
data class TabItem(
    @SerializedName("id") val id: String?,
    @SerializedName("text") val title: String?,
    @SerializedName("is_selected") var isSelected: Boolean?,
)

@Keep
data class Cta(
    @SerializedName("title") val title: String?,
    @SerializedName("text_color") val titleColor: String?,
    @SerializedName("deeplink") val deepLink: String?,
    @SerializedName("bg_color") val backgroundColor: String?,
    @SerializedName("stroke_color") val strokeColor: String?
)

