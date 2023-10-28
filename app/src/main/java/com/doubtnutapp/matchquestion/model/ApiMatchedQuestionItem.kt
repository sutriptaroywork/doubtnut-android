package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 01/10/20.
 */

@Keep
data class ApiMatchedQuestionItem(
    @SerializedName("_id") val id: String,
    @SerializedName("class") val clazz: String?,
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("question_thumbnail") val questionThumbnail: String,
    @SerializedName("question_thumbnail_localized") val questionThumbnailLocalized: String?,
    @SerializedName("_source") val source: ApiMatchedQuestionSource?,
    @SerializedName("canvas") val canvas: ApiCanvas?,
    @SerializedName("html") val html: String?,
    @SerializedName("resource_type") val resourceType: String,
    @SerializedName("video_resource") val resource: ApiVideoResource?,
    @SerializedName("answer_id") val answerId: Long?,
    @SerializedName("widget_data") val widgetData: WidgetEntityModel<WidgetData, WidgetAction>?,
)

@Keep
data class ApiCanvas(
    @SerializedName("background_color") val backgroundColor: String?,
    @SerializedName("padding") val padding: UiConfigPadding?,
    @SerializedName("margin") val margin: UiConfigMargin?,
    @SerializedName("corner_radius") val cornerRadius: UiConfigCornerRadius?,
    @SerializedName("stroke_color") val strokeColor: String?,
    @SerializedName("stroke_width") val strokeWidth: Int?
)