package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class MatchCarousalsData(
    @SerializedName("live_classes") val liveClasses: List<WidgetEntityModel<WidgetData, WidgetAction>>?,
    @SerializedName("live_classes_v2") val liveClassesV2: LiveClassesV2Data?,
    @SerializedName("vip") val vip: List<WidgetEntityModel<WidgetData, WidgetAction>>?,
)

@Keep
data class LiveClassesV2Data(
    @SerializedName("classes") val classes: List<WidgetEntityModel<WidgetData, WidgetAction>>?,
    @SerializedName("overflow_text") val overflowText: String?,
    @SerializedName("overflow_text_deeplink") val overflowTextDeeplink: String?,
)