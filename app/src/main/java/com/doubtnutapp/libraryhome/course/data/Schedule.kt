package com.doubtnutapp.libraryhome.course.data

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 26/09/20.
 */
class ScheduleWidgetModel : WidgetEntityModel<Schedule, WidgetAction>()

@Keep
data class Schedule(
        @SerializedName("id") val id: String?,
        @SerializedName("tag") val tag: String?,
        @SerializedName("date") val date: String?,
        @SerializedName("day") val day: String?,
        @SerializedName("resources") val resources: List<Resource>?
) : WidgetData() {
    @Keep
    data class Resource(
            @SerializedName("id") val id: String?,
            @SerializedName("title") val title: String?,
            @SerializedName("description") val description: String?,
            @SerializedName("duration") val duration: String?,
            @SerializedName("image_url") val imageUrl: String?,
            @SerializedName("type") val type: String?,
            @SerializedName("color") val color: String?,
            @SerializedName("live_at") val liveAt: String?,
            @SerializedName("payment_deeplink") val paymentDeeplink: String?,
            @SerializedName("state") val state: Int,
            @SerializedName("is_premium") val isPremium: Boolean?,
            @SerializedName("is_vip") val isVip: Boolean?,
            @SerializedName("page") val page: String?,
            @SerializedName("resource_type") val resourceType: String?,
            @SerializedName("pdf_url") val pdfUrl: String?,
            @SerializedName("test_id") val testId: String?)
}

class ScheduleHeaderWidgetModel : WidgetEntityModel<ScheduleHeader, WidgetAction>()

@Keep
data class ScheduleHeader(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?
) : WidgetData()

class ScheduleNoDataWidgetModel : WidgetEntityModel<ScheduleNoData, WidgetAction>()

@Keep
data class ScheduleNoData(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("tag") val tag: String?
) : WidgetData()