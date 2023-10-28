package com.doubtnut.referral.data.entity

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ReferAndEarnLandingPageResponse(
    @SerializedName("widgets") val listWidgets: ArrayList<WidgetEntityModel<*, *>>,
    @SerializedName("cta") val cta: Cta?
) {

    @Keep
    data class Cta(
        @SerializedName("title") val title: String?,
        @SerializedName("message_whatsapp") val messageWhatsapp: String?,
        @SerializedName("image_share") val imageShare: String?
    )
}