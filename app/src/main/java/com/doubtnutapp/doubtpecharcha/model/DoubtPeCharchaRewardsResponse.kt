package com.doubtnutapp.doubtpecharcha.model

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.widgetmanager.widgets.GradientBannerWithActionButtonWidget
import com.google.gson.annotations.SerializedName

data class DoubtPeCharchaRewardsResponse(
    @SerializedName("title") val title: String?,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("rewards_faq_data") val rewardFaqData: GradientBannerWithActionButtonWidget.RewardsData,
    @SerializedName("cta") val cta: Cta?
) {
    data class Cta(
        @SerializedName("title") val title: String?,
        @SerializedName("deeplink") val deeplink: String?
    )
}