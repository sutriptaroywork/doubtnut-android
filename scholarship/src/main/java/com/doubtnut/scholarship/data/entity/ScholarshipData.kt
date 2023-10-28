package com.doubtnut.scholarship.data.entity

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ScholarshipData(
    @SerializedName("scholarship_test_id")
    val scholarshipTestId: String?,
    @SerializedName("test_id")
    val testId: String?,
    @SerializedName("progress")
    val progress: String?,

    @SerializedName("bg_color", alternate = ["background_color"])
    val bgColor: String?,

    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,

    @SerializedName("start_time_in_millis")
    val startTimeInMillis: Long?,

    @SerializedName("sticky_widgets")
    val stickyWidgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("widgets")
    val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("footer_widgets")
    val footerWidgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("bottom_data")
    val bottomData: List<ScholarshipBottomData>?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>? = null,
)

@Keep
data class ScholarshipBottomData(
    @SerializedName("background")
    val background: String?,
    @SerializedName("peek_height")
    val peekHeight: Int?,
    @SerializedName("tab")
    val tab: String?,
    @SerializedName("widgets")
    val widgets: List<WidgetEntityModel<*, *>>?,
)